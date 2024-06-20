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
import net.sf.cglib.beans.BeanCopier;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.StressBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.domain.StressPO;
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
public class StressServiceImpl extends TaskBaseService {

    @Resource
    public ChaosTaskDao taskDao;

    @Resource
    public GrpcChannelService grpcChannelService;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Override
    public String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save stress task param createChaosTaskBo={},pipelineBO={},taskStatus={}", createChaosTaskBo, pipelineBO, taskStatus);
        ChaosTask chaosTask = ChaosTask.of(createChaosTaskBo, taskStatus);
        StressPO stressPO = new StressPO();
        BeanCopier beanCopier = net.sf.cglib.beans.BeanCopier.create(StressBO.class, StressPO.class, false);
        beanCopier.copy(pipelineBO, stressPO, null);
        List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
        Optional.ofNullable(createChaosTaskBo.getPodIpList()).ifPresent(ipList -> {
            ipList.forEach(ip -> {
                InstanceUidAndIP instance = new InstanceUidAndIP();
                instance.setIp(ip);
                instanceUidAndIPList.add(instance);
            });
        });
        stressPO.setInstanceUidAndIPList(instanceUidAndIPList);
        chaosTask.setStressPO(stressPO);
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
            podList = CommonUtil.getExecutedPod(podList, chaosTask.getStressPO().getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
            List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getStressPO().getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
            if (!ips.isEmpty()) {
                return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
            }
        } else if (modeEnum.equals(ModeEnum.ANY)) {
            podList = getPods(chaosTask, podList);
        }

        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);

        List<Future<Map>> futureList = new ArrayList<>();
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            Future<Map> objectFuture = Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                try {
                    StringBuffer memoryStressors = new StringBuffer();
                    if (chaosTask.getStressPO().getVmBytes() != null && chaosTask.getStressPO().getVmBytes() > 0) {
                        memoryStressors.append(CmdConstant.MEM_STRESS_SIZE).append(chaosTask.getStressPO().getVmBytes()).append(chaosTask.getStressPO().getVmUnit()).append(CmdConstant.BLANK_SPACE);
                    }
                    if (chaosTask.getStressPO().getVmTimeOut() != null && chaosTask.getStressPO().getVmTimeOut() > 0) {
                        memoryStressors.append(CmdConstant.MEM_STRESS_TIME).append(chaosTask.getStressPO().getVmTimeOut()).append(chaosTask.getStressPO().getVmTimeOutUnit()).append(CmdConstant.BLANK_SPACE);
                    }
                    if (!memoryStressors.isEmpty()) {
                        memoryStressors.append(CmdConstant.MEM_STRESS_WORKER);
                    }
                    StringBuffer cpuStressors = new StringBuffer();
                    if (chaosTask.getStressPO().getCpuNum() != null && chaosTask.getStressPO().getCpuNum() > 0) {
                        cpuStressors.append(CmdConstant.CPU).append(chaosTask.getStressPO().getCpuNum()).append(CmdConstant.BLANK_SPACE);
                    }
                    if (chaosTask.getStressPO().getCpuTimeOut() != null && chaosTask.getStressPO().getCpuTimeOut() > 0) {
                        cpuStressors.append(CmdConstant.TIMEOUT).append(chaosTask.getStressPO().getCpuTimeOut()).append(chaosTask.getStressPO().getCpuTimeOutUnit()).append(CmdConstant.BLANK_SPACE);
                    }
                    Chaosdaemon.ExecStressRequest stressRequest = null;
                    String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(), chaosTask.getProjectId(), chaosTask.getPipelineId());
                    if (!memoryStressors.isEmpty() && cpuStressors.isEmpty()) {
                        stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setMemoryStressors(memoryStressors.toString()).setTarget(containId).build();
                    } else if (memoryStressors.isEmpty() && !cpuStressors.isEmpty()) {
                        stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setCpuStressors(cpuStressors.toString()).setTarget(containId).build();
                    } else if (!memoryStressors.isEmpty() && !cpuStressors.isEmpty()) {
                        stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setMemoryStressors(memoryStressors.toString()).setCpuStressors(cpuStressors.toString()).setTarget(containId).build();
                    }
                    Chaosdaemon.ExecStressResponse response = ChaosDaemonGrpc.newBlockingStub(grpcPodAndChannel.getChannel()).execStressors(stressRequest);
                    Map<String, Object> map = new HashMap<>();
                    map.put("cpuInstance", response.getCpuInstance());
                    map.put("cpuInstanceUid", response.getCpuInstanceUid());
                    map.put("memoryInstance", response.getMemoryInstance());
                    map.put("memoryInstanceUid", response.getMemoryInstanceUid());
                    return map;
                } catch (Exception e) {
                    log.error("stress task execute error, taskId:{}, ip:{}", chaosTask.getId().toString(), grpcPodAndChannel.getPod().getStatus().getPodIP(), e);
                    return null;
                }
            });
            futureList.add(objectFuture);
        });

        List<Pod> finalPodList = podList;
        CountDownLatch latch = new CountDownLatch(1);
        Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
            List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
            for (int i = 0; i < futureList.size(); i++) {
                try {
                    Map map = (Map) futureList.get(i).get(15000, TimeUnit.MILLISECONDS);
                    if (map == null) {
                        continue;
                    }
                    InstanceUidAndIP instanceUidAndIP = new InstanceUidAndIP();
                    instanceUidAndIP.setIp(finalPodList.get(i).getStatus().getPodIP());
                    instanceUidAndIP.setMemoryInstance(map.get("memoryInstance").toString());
                    instanceUidAndIP.setMemoryInstanceUid(map.get("memoryInstanceUid").toString());
                    instanceUidAndIP.setCpuInstance(map.get("cpuInstance").toString());
                    instanceUidAndIP.setCpuInstanceUid(map.get("cpuInstanceUid").toString());
                    instanceUidAndIPList.add(instanceUidAndIP);
                } catch (Exception e) {
                    log.error("stress task execute error, taskId:{}", chaosTask.getId().toString(), e);
                    //todo 通知该pod重启恢复
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusEnum.actioning.type());
            map.put("stressPO.instanceUidAndIPList", instanceUidAndIPList);
            map.put("instanceAndIps", instanceUidAndIPList);
            map.put("endTime", currentTime + chaosTask.getDuration());
            map.put("updateUser", chaosTask.getUpdateUser());
            map.put("executedTimes", chaosTask.getExecutedTimes());
            taskDao.update(chaosTask.getId(), map);
            latch.countDown();
        });
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("latch await error,e:", e);
        }
        // 时间轮终止任务
        wheelTimer(chaosTask);
        return chaosTask.getId().toString();
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return "pod列表为空";
        }
        List<InstanceUidAndIP> instanceUidAndIPList = chaosTask.getStressPO().getInstanceUidAndIPList();
        List<Pod> pods = CommonUtil.getExecutedPod(podList, instanceUidAndIPList.stream().map(InstanceUidAndIP::getIp).collect(Collectors.toList()));
        if (pods.isEmpty()) {
            return "需要恢复的pod列表为空";
        }
        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(pods);
        List<Future> futureList = new ArrayList<>();
        grpcPodAndChannels.forEach(grpcPodAndChannel -> {
            Future<Integer> objectFuture = Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                Optional<InstanceUidAndIP> instance = instanceUidAndIPList.stream().filter(instanceUidAndIP -> instanceUidAndIP.getIp().equals(grpcPodAndChannel.getPod().getStatus().getPodIP())).findFirst();
                Chaosdaemon.CancelStressRequest request = null;
                if (StringUtils.isNotEmpty(instance.get().getMemoryInstance()) && StringUtils.isEmpty(instance.get().getCpuInstance())) {
                    request = Chaosdaemon.CancelStressRequest.newBuilder().setMemoryInstance(instance.get().getMemoryInstance()).setMemoryInstanceUid(instance.get().getMemoryInstanceUid()).build();
                } else if (StringUtils.isEmpty(instance.get().getMemoryInstance()) && StringUtils.isNotEmpty(instance.get().getCpuInstance())) {
                    request = Chaosdaemon.CancelStressRequest.newBuilder().setCpuInstance(instance.get().getCpuInstance()).setCpuInstanceUid(instance.get().getCpuInstanceUid()).build();
                } else if (StringUtils.isNotEmpty(instance.get().getMemoryInstance()) && StringUtils.isNotEmpty(instance.get().getCpuInstance())) {
                    request = Chaosdaemon.CancelStressRequest.newBuilder().setMemoryInstance(instance.get().getMemoryInstance()).setMemoryInstanceUid(instance.get().getMemoryInstanceUid()).setCpuInstance(instance.get().getCpuInstance()).setCpuInstanceUid(instance.get().getCpuInstanceUid()).build();
                }
                try {
                    ChaosDaemonGrpc.newBlockingStub(grpcPodAndChannel.getChannel()).cancelStressors(request);
                } catch (Exception e) {
                    log.error("cancel stress error, taskId:{}, ip:{}", chaosTask.getId().toString(), instance.get().getIp(), e);
                    //todo 通知该pod重启恢复
                    return 0;
                }
                return 1;
            });
            futureList.add(objectFuture);
        });
        for (int i = 0; i < futureList.size(); i++) {
            try {
                futureList.get(i).get(15000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("cancel stress error, taskId:{}", chaosTask.getId().toString(), e);
                //todo 通知该pod重启恢复
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusEnum.recovered.type());
        taskDao.update(chaosTask.getId(), map);
        return chaosTask.getId().toString();
    }
}
