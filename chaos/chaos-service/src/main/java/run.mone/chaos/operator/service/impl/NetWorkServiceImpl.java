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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.NetworkBO;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.domain.NetworkPO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.service.GrpcChannelService;
import run.mone.chaos.operator.service.TaskBaseService;
import run.mone.chaos.operator.util.CommonUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-11 14:57
 */
@Slf4j
@Service
public class NetWorkServiceImpl extends TaskBaseService {

    @Resource
    public ChaosTaskDao taskDao;

    @Resource
    public GrpcChannelService grpcChannelService;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Override
    public String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save network task param createChaosTaskBo={},pipelineBO={},taskStatus={}", createChaosTaskBo, pipelineBO, taskStatus);
        ChaosTask chaosTask = ChaosTask.of(createChaosTaskBo, taskStatus);
        NetworkBO networkBO = new NetworkBO();
        BeanUtils.copyProperties(pipelineBO, networkBO);
        NetworkPO networkPO = new NetworkPO();
        networkPO.setDelayTime(networkBO.getDelayTime());
        networkPO.setLossPercent(networkBO.getLossPercent());
        networkPO.setCorruptionPercent(networkBO.getCorruptionPercent());
        networkPO.setDuplicatesPercent(networkBO.getDuplicatesPercent());
        List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
        Optional.ofNullable(createChaosTaskBo.getPodIpList()).ifPresent(ipList -> {
            ipList.forEach(ip -> {
                InstanceUidAndIP instance = new InstanceUidAndIP();
                instance.setIp(ip);
                instanceUidAndIPList.add(instance);
            });
        });
        networkPO.setInstanceUidAndIPList(instanceUidAndIPList);
        chaosTask.setNetworkPO(networkPO);
        chaosTask.setEndTime(Long.MAX_VALUE);
        taskDao.insert(chaosTask);
        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String execute(ChaosTask chaosTask) {
        Long currentTime = System.currentTimeMillis();

        if (ObjectUtils.isEmpty(chaosTask)) {
            return "chaosTask is null";
        }
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
        if (ObjectUtils.isEmpty(modeEnum)) {
            return "task mode is null";
        }

        if (modeEnum.equals(ModeEnum.APPOINT)) {
            podList = CommonUtil.getExecutedPod(podList, chaosTask.getNetworkPO().getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
            List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getNetworkPO().getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
            if (!ips.isEmpty()) {
                return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
            }
        } else if (modeEnum.equals(ModeEnum.ANY)) {
            podList = getPods(chaosTask, podList);

        }

        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);

        List<Future<String>> futureList = new ArrayList<>();

        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            Future<String> objectFuture = Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                Chaosdaemon.Netem netem = null;
                if (chaosTask.getNetworkPO().getDelayTime() != null && chaosTask.getNetworkPO().getDelayTime() > 0) {
                    netem = Chaosdaemon.Netem.newBuilder().setTime(chaosTask.getNetworkPO().getDelayTime() * 1000).build();
                } else if (chaosTask.getNetworkPO().getLossPercent() != null && chaosTask.getNetworkPO().getLossPercent() > 0) {
                    netem = Chaosdaemon.Netem.newBuilder().setLoss(chaosTask.getNetworkPO().getLossPercent()).build();
                } else if (chaosTask.getNetworkPO().getCorruptionPercent() != null && chaosTask.getNetworkPO().getCorruptionPercent() > 0) {
                    netem = Chaosdaemon.Netem.newBuilder().setCorrupt(chaosTask.getNetworkPO().getCorruptionPercent()).build();
                } else if (chaosTask.getNetworkPO().getDuplicatesPercent() != null && chaosTask.getNetworkPO().getDuplicatesPercent() > 0) {
                    netem = Chaosdaemon.Netem.newBuilder().setDuplicate(chaosTask.getNetworkPO().getDuplicatesPercent()).build();
                }

                Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
                String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(), chaosTask.getProjectId(), chaosTask.getPipelineId());
                Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();

                com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(grpcPodAndChannel.getChannel()).setTcs(tcsRequest);
                return containId;
            });
            futureList.add(objectFuture);
        });

        List<Pod> finalPodList = podList;
        CountDownLatch latch = new CountDownLatch(1);
        Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
            List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
            for (int i = 0; i < futureList.size(); i++) {
                try {
                    InstanceUidAndIP instanceUidAndIP = new InstanceUidAndIP();
                    instanceUidAndIP.setIp(finalPodList.get(i).getStatus().getPodIP());
                    instanceUidAndIPList.add(instanceUidAndIP);
                    String containerId = futureList.get(i).get(15000, TimeUnit.MILLISECONDS);
                    instanceUidAndIP.setContainerId(containerId);
                } catch (Exception e) {
                    log.error("network task execute error, taskId:{}", chaosTask.getId().toString(), e);
                    //todo 通知该pod重启恢复
                }
            }
            Integer status = futureList.isEmpty() ? StatusEnum.fail.type() : StatusEnum.actioning.type();
            Map<String, Object> map = new HashMap<>();
            map.put("status", status);
            map.put("networkPO.instanceUidAndIPList", instanceUidAndIPList);
            map.put("instanceAndIps", instanceUidAndIPList);
            map.put("endTime", currentTime + chaosTask.getDuration());
            map.put("updateUser", chaosTask.getUpdateUser());
            map.put("executedTimes", chaosTask.getExecutedTimes());
            taskDao.update(chaosTask.getId(), map);
            latch.countDown();
        });
        // 时间轮终止任务
        wheelTimer(chaosTask);

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("latch await error,e:", e);
        }

        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        List<InstanceUidAndIP> instanceUidAndIPList = chaosTask.getNetworkPO().getInstanceUidAndIPList();
        List<Pod> pods = CommonUtil.getExecutedPod(podList, instanceUidAndIPList.stream().map(InstanceUidAndIP::getIp).collect(Collectors.toList()));
        if (pods.isEmpty()) {
            return "需要恢复的pod列表为空";
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(pods);
        List<Future> futureList = new ArrayList<>();
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            Future<Integer> objectFuture = Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                Optional<InstanceUidAndIP> instance = instanceUidAndIPList.stream().filter(instanceUidAndIP -> instanceUidAndIP.getIp().equals(grpcPodAndChannel.getPod().getStatus().getPodIP())).findFirst();
                Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().clearTime().build();
                Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
                String containId = instance.get().getContainerId();
                Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();
                try {
                    com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(grpcPodAndChannel.getChannel()).setTcs(tcsRequest);
                    log.info("", empty);
                } catch (Exception e) {
                    log.error("network cancel error, taskId:{}, ip:{}", chaosTask.getId().toString(), instance.get().getIp(), e);
                }
                return null;
            });
            futureList.add(objectFuture);
        });
        for (int i = 0; i < futureList.size(); i++) {
            try {
                futureList.get(i).get(15000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("cancel network error, taskId:{}", chaosTask.getId().toString(), e);
                //todo 通知该pod重启恢复
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusEnum.recovered.type());
        taskDao.update(chaosTask.getId(), map);
        return chaosTask.getId().toString();
    }
}
