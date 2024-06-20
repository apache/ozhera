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
package run.mone.chaos.operator.service;

import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import com.xiaomi.youpin.docean.anno.Service;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.http.HttpBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.HttpPO;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author caobaoyu
 * @description:
 * @date 2023-12-15 09:44
 */
@Slf4j
@Service
public class HttpChaosService {

    @Resource
    private KubernetesClient kubernetesClient;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private ChaosTaskDao taskDao;

    @Resource
    private GrpcChannelService grpcChannelService;

    private static final Gson gson = new Gson();

    public String httpDo(HttpBO httpBO) {
        try {
            Map<String, Object> preMap = new HashMap<>();
            preMap.put("projectId", httpBO.getProjectId());
            preMap.put("pipelineId", httpBO.getPipelineId());
            preMap.put("taskType", TaskEnum.http.type());
            preMap.put("status", StatusEnum.recovered.type());
            ChaosTask latestByKvMap = taskDao.getLatestByKvMap(preMap);
            List<InstanceUidAndIP> instanceAndIps = null;
            if (latestByKvMap != null) {
                instanceAndIps = latestByKvMap.getHttpPO().getInstanceAndIps();
            }
            ChaosTask chaosTask = ChaosTask.of(httpBO, TaskEnum.http.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), httpBO.getExperimentName());
            HttpPO httpPO = new HttpPO();
            httpPO.setRules(httpBO.getRules());
            httpPO.setIsRecover(false);
            chaosTask.setHttpPO(httpPO);
            Object id = taskDao.insert(chaosTask);

            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", httpBO.getPipelineId().toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "pod列表为空";
            }
            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }
            // 根据不同mode做不同操作
            ModeEnum modeEnum = ModeEnum.fromType(httpBO.getMode());
            switch (modeEnum) {
                case ANY:
                    httpApplyRandom(httpBO, httpPO, chaosTask, id, grpcPodAndChannels, instanceAndIps);
                    break;
                case ALL:
                    httpApplyAll(httpBO, httpPO, chaosTask, id, grpcPodAndChannels,instanceAndIps);
                    break;
                case APPOINT:
                    httpApplyAppoint(httpBO, httpPO, chaosTask, id, grpcPodAndChannels,instanceAndIps);
                    break;
                case null, default:
                    return "Invalid mode!";
            }
            return id.toString();

        } catch (Exception e) {
            log.error("HttpChaosService.httpDo error:{}", e);
            return e.getMessage();
        }
    }

    private void updateStatus(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<InstanceUidAndIP> instanceUidAndIPs, Integer status) {
        // recover类型不更新po
        if (status == StatusEnum.actioning.type()) {
            httpPO.setInstanceAndIps(instanceUidAndIPs);
            chaosTask.setHttpPO(httpPO);
        }
        chaosTask.setStatus(status);
        Map<String, Object> map = new HashMap<>(2);
        map.put("status", status);
        map.put("httpPO", httpPO);
        int update = taskDao.update(id, map);
        log.info("httpChaosService.updateStatus:{},httpBO:{}", update, httpBO);
    }

    public String httpRecover(HttpBO httpBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", httpBO.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        try {
            ChaosTask chaosTaskFromDb = taskDao.getById(httpBO.getId(), ChaosTask.class);
            if (chaosTaskFromDb == null) {
                return "该pod未执行过此混沌实验,无需恢复!";
            }
            if (chaosTaskFromDb.getStatus() != StatusEnum.actioning.type()) {
                return "异常：实验状态不为actioned,请联系xxx!";
            }
            List<InstanceUidAndIP> instanceAndIps = chaosTaskFromDb.getHttpPO().getInstanceAndIps();
            HttpPO httpPO = chaosTaskFromDb.getHttpPO();
            ObjectId id = chaosTaskFromDb.getId();
            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }
            ModeEnum modeEnum = ModeEnum.fromType(httpBO.getMode());
            switch (modeEnum) {
                case ANY:
                    httpRecoverRandom(httpBO,httpPO,chaosTaskFromDb,id,grpcPodAndChannels,instanceAndIps);
                    break;
                case ALL:
                    httpRecoverAll(httpBO,httpPO,chaosTaskFromDb,id,grpcPodAndChannels,instanceAndIps);
                    break;
                case APPOINT:
                    httpRecoverAppoint(httpBO,httpPO,chaosTaskFromDb,id,grpcPodAndChannels,instanceAndIps);
                    break;
                case null, default:
                    return "Invalid mode!";
            }
            return "ok";
        } catch (Exception e) {
            log.error("HttpChaosService.httpRevocer error:{}", e);
            return e.getMessage();
        }
    }

    private void httpApplyRandom(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                                 List<InstanceUidAndIP> instanceAndIps) {
        Random random = new Random(System.currentTimeMillis());
        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        String instanceUid = null;
        if (instanceAndIps != null && !instanceAndIps.isEmpty()) {
            instanceUid = instanceAndIps.getFirst().getInstanceUid();
        }
        Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), httpBO, instanceUid);
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(), grpcPodAndChannel.getPod().getStatus().getPodIP()));
        updateStatus(httpBO, httpPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void httpApplyAll(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                              List<InstanceUidAndIP> instanceAndIps) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            String instanceUid = null;
            if (instanceAndIps != null && !instanceAndIps.isEmpty()) {
                instanceUid = getInstanceUidByPodAndChannel(grpcPodAndChannel,instanceAndIps);
            }
            Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), httpBO, instanceUid);
            instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(), grpcPodAndChannel.getPod().getStatus().getPodIP()));
        });
        updateStatus(httpBO, httpPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void httpApplyAppoint(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                                  List<InstanceUidAndIP> instanceAndIps) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        grpcPodAndChannels.forEach(grpcPodAndChannel ->{
            String instanceUid = null;
            if (instanceAndIps != null && !instanceAndIps.isEmpty()) {
                // 只获取instanceAndIps里的ip进行恢复
                instanceUid = getInstanceUidByPodAndChannel(grpcPodAndChannel,instanceAndIps);
            }
            Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpGrpc(grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), httpBO, instanceUid);
            instanceUidAndIPs.add(new InstanceUidAndIP(applyHttpChaosResponse.getInstanceUid(), grpcPodAndChannel.getPod().getStatus().getPodIP()));
        });
        updateStatus(httpBO, httpPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void httpRecoverRandom(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                                   List<InstanceUidAndIP> instanceAndIps) {
        // 遍历，正常应该只有一个需要pod做恢复
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (podAndChannel.getPod().getStatus().getPodIP().equals(instanceAndIps.getFirst().getIp())) {
                Chaosdaemon.ApplyHttpChaosResponse applyHttpChaosResponse = httpRecoverGrpc(podAndChannel.getPod(),
                        podAndChannel.getChannel(), httpBO, instanceAndIps.getFirst().getInstanceUid());

            }
        });
        updateStatus(httpBO, httpPO, chaosTask, id, instanceAndIps, StatusEnum.recovered.type());
    }

    private void httpRecoverAll(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                                List<InstanceUidAndIP> instanceAndIps) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            httpRecoverGrpc(podAndChannel.getPod(), podAndChannel.getChannel(), httpBO, instanceUid);
        });
        updateStatus(httpBO, httpPO, chaosTask, id, instanceAndIps, StatusEnum.recovered.type());
    }

    private void httpRecoverAppoint(HttpBO httpBO, HttpPO httpPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels,
                                    List<InstanceUidAndIP> instanceAndIps) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            httpRecoverGrpc(podAndChannel.getPod(), podAndChannel.getChannel(), httpBO, instanceUid);
        });
        updateStatus(httpBO, httpPO, chaosTask, id, instanceAndIps, StatusEnum.recovered.type());
    }

    private Chaosdaemon.ApplyHttpChaosResponse httpGrpc(Pod pod, ManagedChannel channel, HttpBO httpBO, String instanceUId) {
        log.info("begin httpGrpc podName: {}", pod.getMetadata().getName());
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        AtomicReference<Chaosdaemon.ApplyHttpChaosResponse> applyHttpChaosResponse = new AtomicReference<>();
        containerStatuses.forEach(containerStatus -> {
            String containerID = containerStatus.getContainerID();
            //构建grpc入参
            Chaosdaemon.ApplyHttpChaosRequest httpChaosRequest;
            Chaosdaemon.ApplyHttpChaosRequest.Builder builder = Chaosdaemon.ApplyHttpChaosRequest.newBuilder()
                    .setContainerId(containerID).setEnterNS(true).setRules(gson.toJson(httpBO.getRules()))
                    .addAllProxyPorts(httpBO.getProxy_ports());
            //如果instanceUID不为空，则带入
            if (StringUtils.isBlank(instanceUId)) {
                httpChaosRequest = builder.build();
            } else {
                httpChaosRequest = builder.setInstanceUid(instanceUId).build();
            }
            log.info("httpGRpc req: {}", httpChaosRequest);
            //发起grpc请求
            applyHttpChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel)
                    .applyHttpChaos(httpChaosRequest));

            log.info("httpGrpc res :{}", applyHttpChaosResponse);
            if (applyHttpChaosResponse.get().getStatusCode() == 200) {
                log.info("httpChaos success httpBo: {}", httpBO);
            } else {
                log.info("httpChaos error httpBo: {},error :{}", httpBO, applyHttpChaosResponse.get().getError());
            }
        });
        return applyHttpChaosResponse.get();
    }

    private Chaosdaemon.ApplyHttpChaosResponse httpRecoverGrpc(Pod pod, ManagedChannel channel, HttpBO httpBO, String instanceUId) {
        log.info("begin httpRecoverGrpc podName: {}", pod.getMetadata().getName());
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        AtomicReference<Chaosdaemon.ApplyHttpChaosResponse> applyHttpChaosResponse = new AtomicReference<>();
        containerStatuses.forEach(containerStatus -> {
            String containerID = containerStatus.getContainerID();
            //构建grpc入参
            Chaosdaemon.ApplyHttpChaosRequest httpChaosRequest;
            httpChaosRequest = Chaosdaemon.ApplyHttpChaosRequest.newBuilder()
                    .setContainerId(containerID).setEnterNS(true).setRules(gson.toJson(httpBO.getRules())).setInstanceUid(instanceUId).build();
            log.info("httpRecoverGrpc req: {}", httpChaosRequest);
            //发起grpc请求
            applyHttpChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel)
                    .applyHttpChaos(httpChaosRequest));
            log.info("httpRecoverGrpc res :{}", applyHttpChaosResponse);
            if (applyHttpChaosResponse.get().getStatusCode() == 200) {
                log.info("httpRecoverGrpc success httpBo: {}", httpBO);
            } else {
                log.info("httpRecoverGrpc error httpBo: {},error :{}", httpBO, applyHttpChaosResponse.get().getError());
            }
        });
        return applyHttpChaosResponse.get();
    }

    private String getInstanceUidByPodAndChannel(GrpcPodAndChannel grpcPodAndChannel,List<InstanceUidAndIP> instanceAndIp) {
        String podIP = grpcPodAndChannel.getPod().getStatus().getPodIP();
        AtomicReference<String> res = new AtomicReference<>("");
        instanceAndIp.forEach( it -> {
            if (it.getIp().equals(podIP)) {
                res.set(it.getInstanceUid());
                return;
            }
        });
        return res.get();
    }
}
