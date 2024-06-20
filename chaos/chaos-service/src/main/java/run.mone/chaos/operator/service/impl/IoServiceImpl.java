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
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.IOBO;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.IOPO;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.dto.io.IOChaosActionDTO;
import run.mone.chaos.operator.dto.io.IOFaultDTO;
import run.mone.chaos.operator.dto.io.IOFilterDTO;
import run.mone.chaos.operator.service.GrpcChannelService;
import run.mone.chaos.operator.service.TaskBaseService;
import run.mone.chaos.operator.util.CommonUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-11 14:59
 */
@Slf4j
@Service
public class IoServiceImpl extends TaskBaseService {

    @Resource
    private ChaosTaskDao taskDao;

    @Resource
    private GrpcChannelService grpcChannelService;
    @javax.annotation.Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Override
    public String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save io task param createChaosTaskBo={},pipelineBO={},taskStatus={}", createChaosTaskBo, pipelineBO, taskStatus);
        ChaosTask chaosTask = ChaosTask.of(createChaosTaskBo, taskStatus);
        IOBO ioBO = new IOBO();
        BeanUtils.copyProperties(pipelineBO, ioBO);

        IOPO ioPO = new IOPO();
        BeanUtils.copyProperties(ioBO, ioPO);

        // TODO:后续扩展多种类型
        if ("/home/work/log".equals(ioPO.getVolume())) {
            String path = StringUtils.trimLeadingCharacter(ioPO.getPath(), '/');
            ioPO.setPath("/home/work/log/" + path);
        }

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
        ioPO.setInstanceAndIps(instanceUidAndIPList);
        // ioPO.setType("latency");
        ioPO.setIsRecover(false);
        chaosTask.setInstanceAndIps(instanceUidAndIPList);
        chaosTask.setIoPO(ioPO);
        return taskDao.insert(chaosTask).toString();
    }

    @Override
    @GenerateReport
    public String execute(ChaosTask chaosTask) {
        try {
            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId()
                    .toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }
            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }
            ObjectId id = chaosTask.getId();
            IOPO ioPO = chaosTask.getIoPO();
            // 根据不同mode做不同操作
            ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
            if (ObjectUtils.isEmpty(modeEnum)) {
                return "task mode is null";
            }
            if (modeEnum.equals(ModeEnum.APPOINT)) {
                List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getIoPO().getInstanceAndIps()
                        .stream().map(InstanceUidAndIP::getIp).toList());
                if (!ips.isEmpty()) {
                    return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
                }
            }
            switch (modeEnum) {
                case ANY -> ioApplyRandom(ioPO, chaosTask, id, grpcPodAndChannels);
                case ALL -> ioApplyAll(ioPO, chaosTask, id, grpcPodAndChannels);
                case APPOINT -> ioApplyAppoint(ioPO, chaosTask, id, grpcPodAndChannels);
                default -> {
                    return "Invalid mode!";
                }
            }
            // 时间轮终止任务
            wheelTimer(chaosTask);
            return id.toString();
        } catch (Exception e) {
            log.error("IoChaosService.latency error:{}", e);
            return e.getMessage();
        }
    }

    private void updateStatus(IOPO ioPO, ChaosTask chaosTask, Object id, List<InstanceUidAndIP> instanceUidAndIPs, Integer status) {
        // recover类型不更新po
        if (status == StatusEnum.actioning.type()) {
            ioPO.setInstanceAndIps(instanceUidAndIPs);
            ioPO.setStartTime(System.currentTimeMillis());
            chaosTask.setIoPO(ioPO);
        }
        chaosTask.setStatus(status);
        chaosTask.setEndTime(System.currentTimeMillis() + chaosTask.getDuration());
        chaosTask.setInstanceAndIps(instanceUidAndIPs);
        Object insert = taskDao.insert(chaosTask);
        log.info("ioChaosService.updateStatus:{},ioPO:{}", insert.toString(), ioPO);
    }

    private void ioApplyRandom(IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        Random random = new Random(System.currentTimeMillis());
        // 只执行给定容器
        List<GrpcPodAndChannel> selectedChannels = getSelectedChannels(chaosTask, grpcPodAndChannels, random);

        AtomicBoolean isSuccess = new AtomicBoolean(false);
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        selectedChannels.forEach(grpcPodAndChannel -> {
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(grpcPodAndChannel.getPod(), ioPO,
                    grpcPodAndChannel.getChannel(), containId);

            instanceUidAndIPs.add(new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(), grpcPodAndChannel.getPod()
                    .getStatus().getPodIP()));
            if (!applyIOChaosResponse.getInstanceUid().isEmpty()) {
                isSuccess.set(true);
            }
        });
        updateStatus(ioPO, chaosTask, id, instanceUidAndIPs, isSuccess.get() ? StatusEnum.actioning.type() : StatusEnum.fail.type());
    }

    private void ioApplyAll(IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        grpcPodAndChannels.forEach(podAndChannel -> {
            // 只执行给定容器
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(podAndChannel.getPod(), ioPO,
                    podAndChannel.getChannel(), containId);
            InstanceUidAndIP iopoInstanceUidAndIP = new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(),
                    podAndChannel.getPod().getStatus().getPodIP());
            instanceUidAndIPs.add(iopoInstanceUidAndIP);
            if (!applyIOChaosResponse.getInstanceUid().isEmpty()) {
                isSuccess.set(true);
            }
        });

        if (isSuccess.get()) {
            updateStatus(ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
        } else {
            updateStatus(ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.fail.type());
        }
    }

    private void ioApplyAppoint(IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        List<String> ips = chaosTask.getIoPO().getInstanceAndIps().stream().map(InstanceUidAndIP::getIp).toList();
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (ips.contains(podAndChannel.getPod().getStatus().getPodIP())) {
                // 只执行给定容器
                String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(podAndChannel.getPod(), ioPO,
                        podAndChannel.getChannel(), containId);
                InstanceUidAndIP iopoInstanceUidAndIP = new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(),
                        podAndChannel.getPod().getStatus().getPodIP());
                instanceUidAndIPs.add(iopoInstanceUidAndIP);
                if (!applyIOChaosResponse.getInstanceUid().isEmpty()) {
                    isSuccess.set(true);
                }
            }
        });
        if (isSuccess.get()) {
            updateStatus(ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
        } else {
            updateStatus(ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.fail.type());
        }
    }

    private Chaosdaemon.ApplyIOChaosResponse ioLatency(Pod pod, IOPO ioPO, ManagedChannel channel, String containerID) {
        log.info("begin ioLatency ns :{},name :{},ioPO :{},containerId:{}", pod.getMetadata().getNamespace()
                , pod.getMetadata().getName(), ioPO, containerID);
        //组装请求
        String action = getAction(pod, ioPO);
        AtomicReference<Chaosdaemon.ApplyIOChaosResponse> applyIOChaosResponse = new AtomicReference<>();
        Chaosdaemon.ApplyIOChaosRequest grpcReq = Chaosdaemon.ApplyIOChaosRequest.newBuilder().setActions(action)
                .setEnterNS(true).setVolume(ioPO.getVolume()).setContainerId(containerID).build();
        log.info("ioLatency req :{}", grpcReq);
        applyIOChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel).applyIOChaos(grpcReq));
        log.info("ioLatency res :{}", applyIOChaosResponse);
        return applyIOChaosResponse.get();
    }

    private String getAction(Pod pod, IOPO ioPO) {
        IOChaosActionDTO ioReq = new IOChaosActionDTO();
        IOFaultDTO fault = new IOFaultDTO();
        IOFilterDTO filter = new IOFilterDTO();
        ioReq.setType(ioPO.getType());
        ioReq.setLatency(ioPO.getLatency());
        ioReq.setMethods(ioPO.getMethods());
        ioReq.setPercent(ioPO.getPercent());
        ioReq.setSource(pod.getMetadata().getNamespace() + "/" + pod.getMetadata().getName());
        fault.setErrno(0);
        fault.setWeight(1);
        List<IOFaultDTO> faults = new ArrayList<>();
        faults.add(fault);
        ioReq.setFaults(faults);
        List<IOChaosActionDTO> actions = new ArrayList<>();
        actions.add(ioReq);
        String action = gson.toJson(actions);
        log.info("ioLatency.getAction action:{}", action);
        return action;
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId()
                .toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        try {
            ChaosTask chaosTaskFromDb = taskDao.getById(chaosTask.getId(), ChaosTask.class);
            if (chaosTaskFromDb == null) {
                return "该pod未执行过此混沌实验,无需恢复!";
            }
            if (chaosTaskFromDb.getStatus() != StatusEnum.actioning.type()) {
                return "异常：实验状态不为actioning,请联系xxx!";
            }
            IOPO ioPO = chaosTaskFromDb.getIoPO();
            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }
            List<InstanceUidAndIP> instanceAndIps = ioPO.getInstanceAndIps();

            // 根据不同操作做不同处理
            ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
            if (ObjectUtils.isEmpty(modeEnum)) {
                return "task mode is null";
            }
            List<Pod> pods = CommonUtil.getExecutedPod(podList, instanceAndIps.stream()
                    .map(InstanceUidAndIP::getIp).collect(Collectors.toList()));
            if (pods.isEmpty()) {
                return "需要恢复的pod列表为空";
            }
            switch (modeEnum) {
                case ANY -> ioRecoverRandom(instanceAndIps, grpcPodAndChannels, ioPO, chaosTaskFromDb);
                case ALL -> ioRecoverAll(instanceAndIps, grpcPodAndChannels, ioPO, chaosTaskFromDb);
                case APPOINT -> ioRecoverAppoint(instanceAndIps, grpcPodAndChannels, ioPO, chaosTaskFromDb);
                default -> {
                    return "Invalid mode!";
                }
            }
            return chaosTask.getId().toString();
        } catch (Exception e) {
            log.error("IoChaosService.latencyRecover error:{}", e);
            return e.getMessage();
        }
    }

    private void ioRecoverRandom(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                                 IOPO ioPO, ChaosTask chaosTask) {
        //只恢复，执行过的pod, 正常应该只有一个需要pod做恢复
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (podAndChannel.getPod().getStatus().getPodIP().equals(instanceAndIps.getFirst().getIp())) {
                String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(),
                        ioPO, podAndChannel.getChannel(), instanceAndIps.getFirst().getInstanceUid(), containId);
            }
        });
        updateStatus(ioPO, chaosTask, chaosTask.getId(), instanceAndIps, StatusEnum.recovered.type());
    }

    private void ioRecoverAll(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                              IOPO ioPO, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(),
                    ioPO, podAndChannel.getChannel(), instanceUid, containId);
        });
        updateStatus(ioPO, chaosTask, chaosTask.getId(), instanceAndIps, StatusEnum.recovered.type());
    }

    private void ioRecoverAppoint(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                                  IOPO ioPO, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            // 只获取instanceAndIps里的ip进行恢复
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(),
                    ioPO, podAndChannel.getChannel(), instanceUid, containId);
        });
        updateStatus(ioPO, chaosTask, chaosTask.getId(), instanceAndIps, StatusEnum.recovered.type());
    }

    private Chaosdaemon.ApplyIOChaosResponse ioLatencyRecover(Pod pod, IOPO ioPO, ManagedChannel channel, String instanceUid, String containId) {
        log.info("begin ioLatencyRecover ns :{},name :{},ioPO :{},containId:{}", pod.getMetadata().getNamespace(),
                pod.getMetadata().getName(), ioPO, containId);
        List<IOChaosActionDTO> actions = new ArrayList<>();
        String action = gson.toJson(actions);
        AtomicReference<Chaosdaemon.ApplyIOChaosResponse> applyIOChaosResponse = new AtomicReference<>();
        Chaosdaemon.ApplyIOChaosRequest grpcReq = Chaosdaemon.ApplyIOChaosRequest.newBuilder()
                .setEnterNS(true).setVolume(ioPO.getVolume()).setContainerId(containId)
                .setInstanceUid(instanceUid).setActions(action).build();
        log.info("ioLatencyRecover req :{}", grpcReq);
        applyIOChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel).applyIOChaos(grpcReq));
        log.info("ioLatencyRecover res :{}", applyIOChaosResponse);
        return applyIOChaosResponse.get();
    }

    private String getInstanceUidByPodAndChannel(GrpcPodAndChannel grpcPodAndChannel, List<InstanceUidAndIP> instanceAndIp) {
        String podIP = grpcPodAndChannel.getPod().getStatus().getPodIP();
        AtomicReference<String> res = new AtomicReference<>("");
        instanceAndIp.forEach(it -> {
            if (it.getIp().equals(podIP)) {
                res.set(it.getInstanceUid());
                return;
            }
        });
        return res.get();
    }

}
