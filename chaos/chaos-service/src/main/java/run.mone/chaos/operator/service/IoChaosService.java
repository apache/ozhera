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
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.IOBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.IOPO;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.dto.io.IOChaosActionDTO;
import run.mone.chaos.operator.dto.io.IOFaultDTO;
import run.mone.chaos.operator.dto.io.IOFilterDTO;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/5 11:28
 */

@Slf4j
@Service
public class IoChaosService {

    private static final Gson gson = new Gson();

    @Resource
    private ChaosTaskDao taskDao;

    @Value("${chaos.daemon.url}")
    private String daemonUrl;

    @Value("${chaos.daemon.port}")
    private String daemonPort;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private GrpcChannelService grpcChannelService;

    public String latency(IOBO ioBO) {
        ChaosTask chaosTask = ChaosTask.of(ioBO, TaskEnum.io.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), ioBO.getExperimentName());
        IOPO ioPO = new IOPO();
        ioPO.setType("latency");
        ioPO.setIsRecover(false);
        chaosTask.setIoPO(ioPO);
        Object id = taskDao.insert(chaosTask);
        try {
            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", ioBO.getPipelineId().toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }
            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }
            // 根据不同mode做不同操作
            ModeEnum modeEnum = ModeEnum.fromType(ioBO.getMode());
            switch (modeEnum) {
                case ANY:
                    ioApplyRandom(ioBO, ioPO, chaosTask, id, grpcPodAndChannels);
                    break;
                case ALL:
                    ioApplyAll(ioBO, ioPO, chaosTask, id, grpcPodAndChannels);
                    break;
                case APPOINT:
                    ioApplyAppoint(ioBO, ioPO, chaosTask, id, grpcPodAndChannels);
                    break;
                case null, default:
                    return "Invalid mode!";
            }
            return id.toString();
        } catch (Exception e) {
            log.error("IoChaosService.latency error:{}", e);
            return e.getMessage();
        }
    }

    private void updateStatus(IOBO ioBO, IOPO ioPO, ChaosTask chaosTask, Object id, List<InstanceUidAndIP> instanceUidAndIPs, Integer status) {
        // recover类型不更新po
        if (status == StatusEnum.actioning.type()) {
            ioPO.setVolume(ioBO.getVolume());
            ioPO.setInstanceAndIps(instanceUidAndIPs);
            ioPO.setStartTime(System.currentTimeMillis());
            chaosTask.setIoPO(ioPO);
        }
        chaosTask.setStatus(status);
        Map<String, Object> map = new HashMap<>(2);
        map.put("status", status);
        map.put("ioPO", ioPO);
        int update = taskDao.update(id, map);
        log.info("ioChaosService.updateStatus:{},ioBo:{}", update, ioBO);
    }

    private void ioApplyRandom(IOBO ioBO, IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        Random random = new Random(System.currentTimeMillis());
        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(grpcPodAndChannel.getPod(), ioBO, grpcPodAndChannel.getChannel());
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        instanceUidAndIPs.add(new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(), grpcPodAndChannel.getPod().getStatus().getPodIP()));
        updateStatus(ioBO, ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void ioApplyAll(IOBO ioBO, IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        grpcPodAndChannels.forEach(podAndChannel -> {
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(podAndChannel.getPod(), ioBO, podAndChannel.getChannel());
            InstanceUidAndIP iopoInstanceUidAndIP = new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(),
                    podAndChannel.getPod().getStatus().getPodIP());
            instanceUidAndIPs.add(iopoInstanceUidAndIP);
        });
        updateStatus(ioBO, ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void ioApplyAppoint(IOBO ioBO, IOPO ioPO, ChaosTask chaosTask, Object id, List<GrpcPodAndChannel> grpcPodAndChannels) {
        if (ioBO.getPodIpList() == null || ioBO.getPodIpList().isEmpty()) {
            log.error("podIpList is empty,ioBO:{}", ioBO);
            return;
        }
        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();
        List<String> ips = ioBO.getPodIpList();
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (ips.contains(podAndChannel.getPod().getStatus().getPodIP())) {
                Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatency(podAndChannel.getPod(), ioBO, podAndChannel.getChannel());
                InstanceUidAndIP iopoInstanceUidAndIP = new InstanceUidAndIP(applyIOChaosResponse.getInstanceUid(),
                        podAndChannel.getPod().getStatus().getPodIP());
                instanceUidAndIPs.add(iopoInstanceUidAndIP);
            }
        });
        updateStatus(ioBO, ioPO, chaosTask, id, instanceUidAndIPs, StatusEnum.actioning.type());
    }

    private void ioRecoverRandom(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                                 IOBO ioBO, IOPO ioPO, ChaosTask chaosTask) {
        //只恢复，执行过的pod, 正常应该只有一个需要pod做恢复
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (podAndChannel.getPod().getStatus().getPodIP().equals(instanceAndIps.getFirst().getIp())) {
                Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(), ioBO,
                        ioPO,podAndChannel.getChannel(),instanceAndIps.getFirst().getInstanceUid());
            }
        });
        updateStatus(ioBO, ioPO, chaosTask, ioBO.getId(), instanceAndIps, StatusEnum.recovered.type());
    }

    private void ioRecoverAll(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                              IOBO ioBO, IOPO ioPO, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(), ioBO, ioPO,podAndChannel.getChannel(),instanceUid);
        });
        updateStatus(ioBO, ioPO, chaosTask, ioBO.getId(), instanceAndIps, StatusEnum.recovered.type());
    }

    private void ioRecoverAppoint(List<InstanceUidAndIP> instanceAndIps, List<GrpcPodAndChannel> grpcPodAndChannels,
                                  IOBO ioBO, IOPO ioPO, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            // 只获取instanceAndIps里的ip进行恢复
            String instanceUid = getInstanceUidByPodAndChannel(podAndChannel, instanceAndIps);
            Chaosdaemon.ApplyIOChaosResponse applyIOChaosResponse = ioLatencyRecover(podAndChannel.getPod(), ioBO, ioPO, podAndChannel.getChannel(), instanceUid);
        });
        updateStatus(ioBO, ioPO, chaosTask, ioBO.getId(), instanceAndIps, StatusEnum.recovered.type());
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

    public String latencyRecover(IOBO ioBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", ioBO.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        try {
            ChaosTask chaosTaskFromDb = taskDao.getById(ioBO.getId(), ChaosTask.class);
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
            ModeEnum modeEnum = ModeEnum.fromType(ioBO.getMode());
            switch (modeEnum) {
                case ANY:
                    ioRecoverRandom(instanceAndIps, grpcPodAndChannels, ioBO, ioPO, chaosTaskFromDb);
                    break;
                case ALL:
                    ioRecoverAll(instanceAndIps, grpcPodAndChannels, ioBO, ioPO, chaosTaskFromDb);
                    break;
                case APPOINT:
                    ioRecoverAppoint(instanceAndIps, grpcPodAndChannels, ioBO, ioPO, chaosTaskFromDb);
                    break;
                case null, default:
                    return "Invalid mode!";
            }
            return "ok";
        } catch (Exception e) {
            log.error("IoChaosService.latencyRecover error:{}", e);
            return e.getMessage();
        }
    }

    private Chaosdaemon.ApplyIOChaosResponse ioLatency(Pod pod, IOBO ioBO, ManagedChannel channel) {
        log.info("begin ioLatency ns :{},name :{},ioBO :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName(), ioBO);
        //组装请求
        String action = getAction(pod, ioBO);
        AtomicReference<Chaosdaemon.ApplyIOChaosResponse> applyIOChaosResponse = new AtomicReference<>();
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        containerStatuses.forEach(containerStatus -> {
            String containerID = containerStatus.getContainerID();
            Chaosdaemon.ApplyIOChaosRequest grpcReq = Chaosdaemon.ApplyIOChaosRequest.newBuilder().setActions(action)
                    .setEnterNS(true).setVolume(ioBO.getVolume()).setContainerId(containerID).build();
            log.info("ioLatency req :{}", grpcReq);
            applyIOChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel).applyIOChaos(grpcReq));
            log.info("ioLatency res :{}", applyIOChaosResponse);
        });
        return applyIOChaosResponse.get();
    }

    private static String getAction(Pod pod, IOBO ioBO) {
        IOChaosActionDTO ioReq = new IOChaosActionDTO();
        IOFaultDTO fault = new IOFaultDTO();
        IOFilterDTO filter = new IOFilterDTO();
        ioReq.setType(ioBO.getType());
        ioReq.setLatency(ioBO.getLatency());
        ioReq.setMethods(ioBO.getMethods());
        ioReq.setPercent(ioBO.getPercent());
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

    private Chaosdaemon.ApplyIOChaosResponse ioLatencyRecover(Pod pod, IOBO ioBO, IOPO ioPO,ManagedChannel channel,String instanceUid) {
        log.info("begin ioLatencyRecover ns :{},name :{},ioBO :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName(), ioBO);
        List<IOChaosActionDTO> actions = new ArrayList<>();
        String action = gson.toJson(actions);
        AtomicReference<Chaosdaemon.ApplyIOChaosResponse> applyIOChaosResponse = new AtomicReference<>();
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        containerStatuses.forEach(containerStatus -> {
            String containerID = containerStatus.getContainerID();
            Chaosdaemon.ApplyIOChaosRequest grpcReq = Chaosdaemon.ApplyIOChaosRequest.newBuilder()
                    // TODO:填充instanceUid
                    .setEnterNS(true).setVolume(ioPO.getVolume()).setContainerId(containerID).setInstanceUid(instanceUid).setActions(action).build();
            log.info("ioLatencyRecover req :{}", grpcReq);
            applyIOChaosResponse.set(ChaosDaemonGrpc.newBlockingStub(channel).applyIOChaos(grpcReq));
            log.info("ioLatencyRecover res :{}", applyIOChaosResponse);
        });
        return applyIOChaosResponse.get();
    }

    public String fault(IOBO ioBO) {
        return null;
    }

    public String attrOverride(IOBO ioBO) {
        return null;
    }

}
