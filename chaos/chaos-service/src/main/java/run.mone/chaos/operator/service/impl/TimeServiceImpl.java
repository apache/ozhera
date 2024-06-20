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

import com.google.protobuf.Empty;
import com.xiaomi.youpin.docean.anno.Service;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.TimeBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.domain.TimePO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
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
 * @description: todo update status
 * @date 2024-04-11 14:59
 */
@Service
@Slf4j
public class TimeServiceImpl extends TaskBaseService {

    @Resource
    private ChaosTaskDao taskDao;

    @Resource
    private GrpcChannelService grpcChannelService;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Override
    public String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save time task param createChaosTaskBo={},pipelineBO={},taskStatus={}", createChaosTaskBo, pipelineBO, taskStatus);
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", createChaosTaskBo.getPipelineId().toString()).list().getItems();
        if (CollectionUtils.isEmpty(podList)) {
            return "podList is empty";
        }
        TimeBO timeBO = new TimeBO();
        BeanUtils.copyProperties(pipelineBO, timeBO);
        ChaosTask chaosTask = ChaosTask.of(createChaosTaskBo, taskStatus);
        TimePO timePO = new TimePO();
        timePO.setTimeOffset(timeBO.getTimeOffset());
        timePO.setClkIdsMask(ObjectUtils.isEmpty(timeBO.getClkIdsMask()) ? 1L : timeBO.getClkIdsMask());
        chaosTask.setTimePO(timePO);
        taskDao.insert(chaosTask);
        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String execute(ChaosTask chaosTask) {
        Long currentTime = System.currentTimeMillis();
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
        if (grpcPodAndChannels.isEmpty()) {
            return "unable to connect any grpc server!";
        }
        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
        if (ObjectUtils.isEmpty(modeEnum)) {
            return "task mode is null";
        }

        List<InstanceUidAndIP> instanceUidAndIPs = new ArrayList<>();

        switch (modeEnum) {
            case ANY -> timeApplyRandom(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels, instanceUidAndIPs);
            case ALL -> timeApplyAll(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels, instanceUidAndIPs);
            case APPOINT -> timeApplyAppoint(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels, instanceUidAndIPs);
        }

        chaosTask.setInstanceAndIps(instanceUidAndIPs);
        chaosTask.setEndTime(chaosTask.getDuration() + currentTime);
        taskDao.insert(chaosTask);
        wheelTimer(chaosTask);
        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        TimePO timePO = chaosTask.getTimePO();
        if (ObjectUtils.isEmpty(timePO)) {
            return "timePO is null";
        }
        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
        if (ObjectUtils.isEmpty(modeEnum)) {
            return "task mode is null";
        }
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
        if (grpcPodAndChannels.isEmpty()) {
            return "unable to connect any grpc server!";
        }
        switch (modeEnum) {
            case ANY -> timeRecoverRandom(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels);
            case ALL -> timeRecoverAll(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels);
            case APPOINT -> timeRecoverAppoint(chaosTask.getTimePO(), chaosTask, grpcPodAndChannels);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusEnum.recovered.type());
        map.put("updateUser", chaosTask.getUpdateUser());
        taskDao.update(chaosTask.getId(), map);
        return chaosTask.getId().toString();
    }

    private void timeRecoverAppoint(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels) {
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            if (grpcPodAndChannel.getPod().getStatus().getPodIP().equals(chaosTask.getInstanceAndIps().getFirst().getIp())) {
                String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                timeRecoverGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId);
            }
        });
    }

    private void timeRecoverAll(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels) {
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            timeRecoverGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId);
        });
    }

    private void timeRecoverRandom(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels) {
        List<String> ipList = chaosTask.getInstanceAndIps().stream().map(InstanceUidAndIP::getIp).toList();
        grpcPodAndChannels.stream().filter(grpcPodAndChannel -> ipList.contains(grpcPodAndChannel.getPod().getStatus().getPodIP()))
                .forEach(grpcPodAndChannel -> {
                    String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                            chaosTask.getProjectId(), chaosTask.getPipelineId());
                    timeRecoverGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId);
                });

    }

    private void timeApplyAppoint(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels, List<InstanceUidAndIP> instanceUidAndIPs) {
        List<Pair<Integer, String>> pairs = new ArrayList<>();
        List<TimePO.IpAndUid> ipAndUids = Lists.newArrayList();
        chaosTask.getPodIpList().forEach(podIp -> {
            Optional<GrpcPodAndChannel> grpcPodAndChannelOptional = grpcPodAndChannels.stream().filter(grpcPodAndChannel -> grpcPodAndChannel.getPod().getStatus().getPodIP().equals(podIp)).findFirst();
            if (grpcPodAndChannelOptional.isEmpty()) {
                throw new RuntimeException("podIp is not exist");
            }
            GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannelOptional.get();
            getInstancsUidAndIps(instanceUidAndIPs, grpcPodAndChannel);

            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Pair<Integer, String> integerStringPair = timeExecuteGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId, ipAndUids);
            pairs.add(integerStringPair);
        });
        timePO.setIpAndUids(ipAndUids);
        chaosTask.setTimePO(timePO);
        if (pairs.stream().anyMatch(pair -> pair.getLeft() == 0)) {
            chaosTask.setStatus(StatusEnum.actioning.type());
        } else {
            chaosTask.setStatus(StatusEnum.fail.type());
        }
    }

    private void timeApplyAll(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels, List<InstanceUidAndIP> instanceUidAndIPs) {
        List<Pair<Integer, String>> pairs = new ArrayList<>();
        List<TimePO.IpAndUid> ipAndUids = Lists.newArrayList();
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            getInstancsUidAndIps(instanceUidAndIPs, grpcPodAndChannel);
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Pair<Integer, String> integerStringPair = timeExecuteGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId, ipAndUids);
            pairs.add(integerStringPair);
        });
        timePO.setIpAndUids(ipAndUids);
        chaosTask.setTimePO(timePO);
        if (pairs.stream().anyMatch(pair -> pair.getLeft() == 0)) {
            chaosTask.setStatus(StatusEnum.actioning.type());
        } else {
            chaosTask.setStatus(StatusEnum.fail.type());
        }
    }

    private void timeApplyRandom(TimePO timePO, ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels, List<InstanceUidAndIP> instanceUidAndIPs) {
        Random random = new Random(System.currentTimeMillis());

        List<GrpcPodAndChannel> selectedChannels = getSelectedChannels(chaosTask, grpcPodAndChannels, random);
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        List<TimePO.IpAndUid> ipAndUids = Lists.newArrayList();
        selectedChannels.forEach(grpcPodAndChannel -> {
            String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            Pair<Integer, String> executeRes = timeExecuteGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId, ipAndUids);
            getInstancsUidAndIps(instanceUidAndIPs, grpcPodAndChannel);
            // 成功一个就是认为注入成功了
            if (executeRes.getLeft() == 0) {
                isSuccess.set(true);
            }
        });
        timePO.setIpAndUids(ipAndUids);
        chaosTask.setTimePO(timePO);
        chaosTask.setStatus(isSuccess.get() ? StatusEnum.actioning.type() : StatusEnum.fail.type());
        //
        //
        //        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        //        getInstancsUidAndIps(instanceUidAndIPs, grpcPodAndChannel);
        //        String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
        //                chaosTask.getProjectId(), chaosTask.getPipelineId());
        //        Pair<Integer, String> executeRes = timeExecuteGrpc(chaosTask.getId().toString(), grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), timePO, containId);
        //        if (executeRes.getLeft() != 0) {
        //            throw new RuntimeException(executeRes.getRight());
        //        }
        //        if (executeRes.getKey().equals(0)) {
        //            chaosTask.setStatus(StatusEnum.actioning.type());
        //        } else {
        //            chaosTask.setStatus(StatusEnum.fail.type());
        //        }


    }


    private void getInstancsUidAndIps(List<InstanceUidAndIP> instanceUidAndIPs, GrpcPodAndChannel grpcPodAndChannel) {
        String containerID = grpcPodAndChannel.getPod().getStatus().getContainerStatuses().get(0).getContainerID();
        instanceUidAndIPs.add(new InstanceUidAndIP(containerID, grpcPodAndChannel.getPod().getStatus().getPodIP()));
    }

    private Pair<Integer, String> timeExecuteGrpc(String uId, Pod pod, ManagedChannel channel, TimePO timePO, String containerId, List<TimePO.IpAndUid> ipAndUids) {
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        try {
            containerStatuses.stream().filter(c -> c.getContainerID().equals(containerId)).forEach(containerStatus -> {

                String uuId = UUID.randomUUID().toString();
                Chaosdaemon.TimeRequest request = Chaosdaemon.TimeRequest.newBuilder()
                        .setContainerId(containerId)
                        .setUid(uuId)
                        .setClkIdsMask(1)
                        .setSec(timePO.getTimeOffset())
                        .build();
                log.info("timeGrpc request:{}", gson.toJson(request));
                Empty empty = ChaosDaemonGrpc.newBlockingStub(channel)
                        .setTimeOffset(request);
                ipAndUids.add(TimePO.IpAndUid.builder().ip(pod.getStatus().getPodIP()).uid(uuId).build());
            });
        } catch (Exception e) {
            log.error("timeGrpc error:", e);
            return Pair.of(-1, "set timeChaos error");
        }
        return Pair.of(0, uId);

    }

    private Pair<Integer, String> timeRecoverGrpc(String uId, Pod pod, ManagedChannel channel, TimePO timePO, String containerId) {
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        try {
            containerStatuses.stream().filter(c -> c.getContainerID().equals(containerId)).forEach(containerStatus -> {
                Map<String, String> collect = timePO.getIpAndUids().stream().collect(Collectors.toMap(TimePO.IpAndUid::getIp, TimePO.IpAndUid::getUid));
                Chaosdaemon.TimeRequest request = Chaosdaemon.TimeRequest.newBuilder()
                        .setContainerId(containerId)
                        .setUid(collect.get(pod.getStatus().getPodIP()))
                        .build();
                log.info("timeRecoverGrpc request:{}", gson.toJson(request));
                Empty empty = ChaosDaemonGrpc.newBlockingStub(channel)
                        .recoverTimeOffset(request);
            });
        } catch (Exception e) {
            log.error("timeRecoverGrpc error:", e);
            return Pair.of(-1, "recover timeChaos error");
        }
        return Pair.of(0, uId);

    }

    private String getInstanceUidByPodAndChannel(GrpcPodAndChannel grpcPodAndChannel, List<InstanceUidAndIP> instanceAndIp) {
        String podIP = grpcPodAndChannel.getPod().getStatus().getPodIP();
        AtomicReference<String> res = new AtomicReference<>("");
        instanceAndIp.forEach(it -> {
            if (it.getIp().equals(podIP)) {
                res.set(it.getInstanceUid());
            }
        });
        return res.get();
    }


}
