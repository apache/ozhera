/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package run.mone.chaos.operator.service.impl;

import com.xiaomi.youpin.docean.anno.Service;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.http.HttpBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.HttpPO;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.service.GrpcChannelService;
import run.mone.chaos.operator.service.TaskBaseService;
import run.mone.chaos.operator.util.CommonUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-11 14:58
 */
@Service
@Slf4j
/**
 * 注意：
 * 1、HTTPChaos 暂不支持注入 HTTPS 连接
 * 2、实验运行时注入故障的优先级（顺序）固定为 abort -> delay -> replace -> patch。其中 abort 故障会导致短路，直接中断此次连接
 * 3、在生产环境下谨慎使用非幂等语义请求（例如大多数 POST 请求）。若使用了这类请求，注入故障后可能无法通过重复请求使目标服务恢复正常状态
 * 4、注入故障后，不能通过本机ip或者localhost自调的方式验证，需要在其他机器上验证
 */
public class HttpServiceImpl extends TaskBaseService {

    @javax.annotation.Resource
    public ChaosTaskDao taskDao;

    @javax.annotation.Resource
    public GrpcChannelService grpcChannelService;

    @javax.annotation.Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Override
    public String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save http task param createChaosTaskBo={},pipelineBO={},taskStatus={}", createChaosTaskBo, pipelineBO, taskStatus);
        ChaosTask chaosTask = ChaosTask.of(createChaosTaskBo, taskStatus);
        HttpBO httpBO = new HttpBO();
        BeanUtils.copyProperties(pipelineBO, httpBO);
        httpBO.setIsRecover(false);
        String s = commonHttpParamCheck(httpBO);
        if (!s.equals("ok")) {
            return s;
        }

        HttpPO httpPO = new HttpPO();
        BeanUtils.copyProperties(httpBO, httpPO);
        List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
        if (createChaosTaskBo.getMode() == ModeEnum.APPOINT.type()) {
            Optional.ofNullable(createChaosTaskBo.getPodIpList()).ifPresent(ipList -> {
                ipList.forEach(ip -> {
                    InstanceUidAndIP instance = new InstanceUidAndIP();
                    instance.setIp(ip);
                    instanceUidAndIPList.add(instance);
                });
            });
        }
        httpPO.setInstanceAndIps(instanceUidAndIPList);
        chaosTask.setInstanceAndIps(instanceUidAndIPList);
        chaosTask.setHttpPO(httpPO);
        return taskDao.insert(chaosTask).toString();
    }

    //abort 中断服务端的连接
    //delay 为目标过程注入延迟
    //replace 替换请求报文或者响应报文的部分内容
    //patch 给请求报文或响应报文添加额外内容
    @Override
    @GenerateReport
    public String execute(ChaosTask chaosTask) {
        Long current = System.currentTimeMillis();
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId()
                .toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
        if (ObjectUtils.isEmpty(modeEnum)) {
            return "task mode is null";
        }
        if (modeEnum.equals(ModeEnum.APPOINT)) {
            podList = podList.stream().filter(pod -> chaosTask.getPodIpList().contains(pod.getStatus().getPodIP())).toList();
            List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getHttpPO()
                    .getInstanceAndIps().stream().map(InstanceUidAndIP::getIp).toList());
            if (!ips.isEmpty()) {
                return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
            }
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
        if (grpcPodAndChannels.isEmpty()) {
            return "unable to connect any grpc server!";
        }
        HttpPO httpPO = chaosTask.getHttpPO();

        /*Map<String, Object> preMap = new HashMap<>();
        preMap.put("projectId", chaosTask.getProjectId());
        preMap.put("pipelineId", chaosTask.getPipelineId());
        preMap.put("taskType", TaskEnum.http.type());
        preMap.put("status", StatusEnum.recovered.type());
        ChaosTask latestByKvMap = taskDao.getLatestByKvMap(preMap);
        List<InstanceUidAndIP> instanceAndIps = null;
        if (latestByKvMap != null) {
            instanceAndIps = latestByKvMap.getHttpPO().getInstanceAndIps();
            httpPO.setInstanceAndIps(instanceAndIps);
        }*/

        chaosTask.setEndTime(current + chaosTask.getDuration());
        switch (modeEnum) {
            case ANY -> httpApplyRandom(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
            case ALL -> httpApplyAll(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
            case APPOINT -> httpApplyAppoint(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
            default -> {
                return "Invalid mode!";
            }
        }
        // 时间轮终止任务
        wheelTimer(chaosTask);
        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        HttpPO httpPO = chaosTask.getHttpPO();
        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());

        if (ObjectUtils.isEmpty(modeEnum)) {
            return "task mode is null";
        }
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id",
                chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        if (modeEnum.equals(ModeEnum.APPOINT)) {
            podList = podList.stream().filter(pod -> chaosTask.getPodIpList().contains(pod.getStatus().getPodIP())).toList();
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
        if (grpcPodAndChannels.isEmpty()) {
            return "unable to connect any grpc server!";
        }
        List<InstanceUidAndIP> instanceUidAndIPList = chaosTask.getHttpPO().getInstanceAndIps();
        List<Pod> pods = CommonUtil.getExecutedPod(podList, instanceUidAndIPList.stream()
                .map(InstanceUidAndIP::getIp).collect(Collectors.toList()));
        if (pods.isEmpty()) {
            return "需要恢复的pod列表为空";
        }
        switch (modeEnum) {
            case ANY -> httpRecoverRandom(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
            case ALL -> httpRecoverAll(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
            case APPOINT -> httpRecoverAppoint(httpPO, chaosTask, grpcPodAndChannels, httpPO.getInstanceAndIps());
        }

        return chaosTask.getId().toString();
    }


    private void httpRecoverRandom(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels,
                                   List<InstanceUidAndIP> instanceAndIps) {
        // 遍历，正常应该只有一个需要pod做恢复
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (podAndChannel.getPod().getStatus().getPodIP().equals(instanceAndIps.getFirst().getIp())) {
                String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpRecoverGrpc(podAndChannel.getPod(),
                        podAndChannel.getChannel(), httpPO, instanceAndIps.getFirst().getInstanceUid(), containId);
            }
        });
        updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.recovered.type());
    }

    private void httpRecoverAll(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels,
                                List<InstanceUidAndIP> instanceAndIps) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            httpRecoverGrpc(podAndChannel.getPod(), podAndChannel.getChannel(), httpPO, instanceUid, containId);
        });
        updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.recovered.type());
    }

    private void httpRecoverAppoint(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels,
                                    List<InstanceUidAndIP> instanceAndIps) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            httpRecoverGrpc(podAndChannel.getPod(), podAndChannel.getChannel(), httpPO, instanceUid, containId);
        });
        updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.recovered.type());
    }


    private Chaosdaemon.ApplyHttpChaosResponse httpRecoverGrpc(Pod pod, ManagedChannel channel, HttpPO httpPO, String instanceUId, String containerId) {
        log.info("begin httpRecoverGrpc podName: {}", pod.getMetadata().getName());
        AtomicReference<Chaosdaemon.ApplyHttpChaosResponse> applyHttpChaosResponse = new AtomicReference<>();
        //构建grpc入参
        Chaosdaemon.ApplyHttpChaosRequest httpChaosRequest;
        httpChaosRequest = Chaosdaemon.ApplyHttpChaosRequest.newBuilder()
                .setContainerId(containerId).setEnterNS(true).setRules("[]").setInstanceUid(instanceUId).build();
        log.info("httpRecoverGrpc req: {}", httpChaosRequest);
        //发起grpc请求
        applyHttpChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel)
                .applyHttpChaos(httpChaosRequest));
        log.info("httpRecoverGrpc res :{}", applyHttpChaosResponse);
        if (applyHttpChaosResponse.get().getStatusCode() == 200) {
            log.info("httpRecoverGrpc success httpPO: {}", httpPO);
        } else {
            log.info("httpRecoverGrpc error httpPO: {},error :{}", httpPO, applyHttpChaosResponse.get().getError());
        }
        return applyHttpChaosResponse.get();
    }


    private void httpApplyRandom(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels, List<InstanceUidAndIP> instanceAndIps) {
        Random random = new Random(System.currentTimeMillis());
        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        String instanceUid = null;
        /*if (instanceAndIps != null && !instanceAndIps.isEmpty()) {

            // 筛选出与当前实验的podIp一致的instanceUid
            instanceAndIps = instanceAndIps.stream().filter(instanceAndIp ->
                    instanceAndIp.getIp().equals(grpcPodAndChannel.getPod().getStatus().getPodIP())).toList();
            if (!instanceAndIps.isEmpty()) {
                instanceUid = instanceAndIps.getFirst().getInstanceUid();
            }
        }*/
        String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                chaosTask.getProjectId(), chaosTask.getPipelineId());
        Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(),
                grpcPodAndChannel.getChannel(), httpPO, instanceUid, containId);
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(),
                grpcPodAndChannel.getPod().getStatus().getPodIP()));
        if (applyHttpChaosResponse.getStatusCode() == 200) {
            updateStatus(httpPO, chaosTask, instanceUidAndIPs, StatusEnum.actioning.type());
        } else {
            updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.fail.type());
        }

    }

    private Chaosdaemon.ApplyHttpChaosResponse httpGrpc(Pod pod, ManagedChannel channel, HttpPO httpPO, String instanceUId, String containId) {
        log.info("begin httpGrpc podName: {},containId:{}", pod.getMetadata().getName(), containId);
        AtomicReference<Chaosdaemon.ApplyHttpChaosResponse> applyHttpChaosResponse = new AtomicReference<>();
        //构建grpc入参
        Chaosdaemon.ApplyHttpChaosRequest httpChaosRequest;
        Chaosdaemon.ApplyHttpChaosRequest.Builder builder = Chaosdaemon.ApplyHttpChaosRequest.newBuilder()
                .setContainerId(containId).setEnterNS(true).setRules(gson.toJson(httpPO.getRules()))
                .addProxyPorts(httpPO.getProxy_ports().getFirst());
        // 修改为自己创建uuid
        httpChaosRequest = builder.setInstanceUid(UUID.randomUUID().toString()).build();
        log.info("httpGRpc req: {}", httpChaosRequest);
        //发起grpc请求
        applyHttpChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel)
                .applyHttpChaos(httpChaosRequest));

        log.info("httpGrpc res :{}", applyHttpChaosResponse);
        if (applyHttpChaosResponse.get().getStatusCode() == 200) {
            log.info("httpChaos success httpPO: {}", httpPO);
        } else {
            log.info("httpChaos error httpPO: {},error :{}", httpPO, applyHttpChaosResponse.get().getError());
        }
        return applyHttpChaosResponse.get();
    }

    private void httpApplyAll(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels,
                              List<InstanceUidAndIP> instanceAndIps) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            String instanceUid = null;
            /*if (instanceAndIps != null && !instanceAndIps.isEmpty()) {
                instanceUid = getInstanceUidByPodAndChannel(grpcPodAndChannel, instanceAndIps);
            }*/
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(),
                    grpcPodAndChannel.getChannel(), httpPO, instanceUid, containId);
            instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(),
                    grpcPodAndChannel.getPod().getStatus().getPodIP()));
            if (applyHttpChaosResponse.getStatusCode() == 200) {
                isSuccess.set(true);
            }
        });
        if (isSuccess.get()) {
            updateStatus(httpPO, chaosTask, instanceUidAndIPs, StatusEnum.actioning.type());
        } else {
            updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.fail.type());
        }
    }

    private void httpApplyAppoint(HttpPO httpPO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels,
                                  List<InstanceUidAndIP> instanceAndIps) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            String instanceUid = null;
            /*if (instanceAndIps != null && !instanceAndIps.isEmpty()) {
                // 只获取instanceAndIps里的ip进行恢复
                instanceUid = getInstanceUidByPodAndChannel(grpcPodAndChannel, instanceAndIps);
            }*/
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(),
                    grpcPodAndChannel.getChannel(), httpPO, instanceUid, containId);
            instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(),
                    grpcPodAndChannel.getPod().getStatus().getPodIP()));

            if (applyHttpChaosResponse.getStatusCode() == 200) {
                isSuccess.set(true);
            }
        });
        if (isSuccess.get()) {
            updateStatus(httpPO, chaosTask, instanceUidAndIPs, StatusEnum.actioning.type());
        } else {
            updateStatus(httpPO, chaosTask, instanceAndIps, StatusEnum.fail.type());
        }
    }


    private void updateStatus(HttpPO httpPO, ChaosTask chaosTask, List<InstanceUidAndIP> instanceUidAndIPs, Integer status) {
        // recover类型不更新po
        if (status == StatusEnum.actioning.type()) {
            httpPO.setInstanceAndIps(instanceUidAndIPs);
            chaosTask.setHttpPO(httpPO);
        }
        chaosTask.setStatus(status);
        chaosTask.setInstanceAndIps(instanceUidAndIPs);
        chaosTask.setEndTime( System.currentTimeMillis() + chaosTask.getDuration());
        taskDao.insert(chaosTask);
        log.info("httpChaosService.updateStatus:{},httpPO:{}", chaosTask.getId().toString(), httpPO);
    }

    private String getInstanceUidByPodAndChannel(GrpcPodAndChannel grpcPodAndChannel, List<InstanceUidAndIP> instanceAndIp) {
        String podIP = grpcPodAndChannel.getPod().getStatus().getPodIP();
        AtomicReference<String> res = new AtomicReference<>(null);
        instanceAndIp.forEach(it -> {
            if (it.getIp().equals(podIP)) {
                res.set(it.getInstanceUid());
            }
        });
        return res.get();
    }


    private String commonHttpParamCheck(HttpBO httpBO) {
        if (httpBO.getIsRecover()) {
            //必须有rules且为空数组
            if (httpBO.getRules() == null || !httpBO.getRules().isEmpty()) {
                return "恢复操作必须为空操作!";
            }
        } else {
            //必须有proxy_ports、rules
            if (httpBO.getProxy_ports() == null || httpBO.getProxy_ports().isEmpty()) {
                return "故障参数非法!";
            }
        }
        return "ok";
    }

}
