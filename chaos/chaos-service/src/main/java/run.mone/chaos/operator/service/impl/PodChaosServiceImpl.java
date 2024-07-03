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

import com.google.common.collect.Maps;
import com.google.protobuf.Empty;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.Pair;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.IOBO;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.PodBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.domain.PodPO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.service.GrpcChannelService;
import run.mone.chaos.operator.service.TaskBaseService;
import run.mone.chaos.operator.util.AnnotationUtil;
import run.mone.chaos.operator.util.CommonUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-11 14:42
 */
@Slf4j
@Service
public class PodChaosServiceImpl extends TaskBaseService {

    @Resource
    private ChaosTaskDao taskDao;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private GrpcChannelService grpcChannelService;

    @Override
    public String save(CreateChaosTaskBo taskBo, PipelineBO pipelineBO, Integer taskStatus) {
        log.info("save pod task param createChaosTaskBo={},pipelineBO={},taskStatus={}", taskBo, pipelineBO, taskStatus);
        ChaosTask chaosTask = ChaosTask.of(taskBo, taskStatus);
        PodBO podBO = new PodBO();
        BeanUtils.copyProperties(pipelineBO, podBO);
        PodPO podPO = new PodPO();
        BeanUtils.copyProperties(podBO, podPO);
        podPO.setPodIpList(podBO.getPodIpList());
        List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
        if (taskBo.getMode() == ModeEnum.APPOINT.type()) {
            Optional.ofNullable(taskBo.getPodIpList()).ifPresent(ipList -> {
                ipList.forEach(ip -> {
                    InstanceUidAndIP instance = new InstanceUidAndIP();
                    instance.setIp(ip);
                    instanceUidAndIPList.add(instance);
                });
            });
        }
        chaosTask.setInstanceAndIps(instanceUidAndIPList);
        podPO.setInstanceUidAndIPList(instanceUidAndIPList);
        chaosTask.setPodPO(podPO);
        taskDao.insert(chaosTask);
        return chaosTask.getId().toString();
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
            PodPO podPO = chaosTask.getPodPO();
            switch (podPO.getType()) {
                case "killContainer" -> {
                    return containerKill(podPO, podList, chaosTask);
                }
                case "killPod" -> {
                    return podKill(podPO, podList, chaosTask);
                }
                case "FailurePod" -> {
                    return podFailure(podPO, podList, chaosTask);
                }
                case null, default -> {
                    return "unknown type";
                }
            }
        } catch (Exception e) {
            log.error("podChaos type: {}  error:{}", chaosTask.getPodPO().getType(), e);
            return "error";
        }
    }

    @Override
    @GenerateReport
    public String recover(ChaosTask chaosTask) {
        try {
            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", chaosTask.getPipelineId()
                    .toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }

            PodPO podPO = chaosTask.getPodPO();
            if (!"FailurePod".equals(podPO.getType())) {
                //恢复目前只有替换镜像的恢复，其他直接返回ok
                Map<String, Object> updateMap = new HashMap<>(1);
                updateMap.put("status", StatusEnum.recovered.type());
                taskDao.update(chaosTask.getId(), updateMap);
                return chaosTask.getId().toString();
            }
            if (chaosTask.getStatus() != StatusEnum.actioning.type()) {
                return "异常：实验状态不为actioning,请联系xxx!";
            }
            ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
            if (modeEnum.equals(ModeEnum.APPOINT)) {
                List<InstanceUidAndIP> instanceUidAndIPList = chaosTask.getPodPO().getInstanceUidAndIPList();
                List<Pod> pods = CommonUtil.getExecutedPod(podList, instanceUidAndIPList.stream()
                        .map(InstanceUidAndIP::getIp).collect(Collectors.toList()));
                if (pods.isEmpty()) {
                    return "需要恢复的pod列表为空";
                } else {
                    podList = pods;
                }
            }
            switch (modeEnum) {
                case ANY:
                case ALL:
                case APPOINT:
                    // 不需要区分mode，里面有检测
                    podList.forEach(pod -> FailureRecover(pod, chaosTask));
                    break;
                case null, default:
                    return "Invalid mode!";
            }

            Map<String, Object> updateMap = new HashMap<>(1);
            updateMap.put("status", StatusEnum.recovered.type());
            taskDao.update(chaosTask.getId(), updateMap);
            return chaosTask.getId().toString();
        } catch (Exception e) {
            log.error("podChaos recover error:{}", e);
            return "error";
        }
    }

    public String containerKill(PodPO podPO, List<Pod> podList, ChaosTask chaosTask) {

        List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
        if (grpcPodAndChannels.isEmpty()) {
            return "unable to connect any grpc server!";
        }

        ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
        if (modeEnum.equals(ModeEnum.APPOINT)) {
            List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getPodPO().getInstanceUidAndIPList()
                    .stream().map(InstanceUidAndIP::getIp).toList());
            if (!ips.isEmpty()) {
                return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
            }
        }
        switch (modeEnum) {
            case ANY -> killRandomContainers(grpcPodAndChannels, chaosTask);
            case ALL -> killAllContainers(podList, grpcPodAndChannels, chaosTask);
            case APPOINT -> {
                if (podPO.getPodIpList() == null || podPO.getPodIpList().isEmpty()) {
                    return "podIpList is empty";
                }
                List<String> ips = chaosTask.getPodPO().getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList();
                killAppointedContainers(ips, grpcPodAndChannels, chaosTask);
            }
            default -> {
                return "Invalid mode!";
            }
        }

        return chaosTask.getId().toString();
    }

    private void updateStatus(List<String> podIp, ChaosTask chaosTask, Integer status) {
        List<InstanceUidAndIP> instanceUidAndIPList = new ArrayList<>();
        podIp.forEach(ip -> {
            InstanceUidAndIP instance = new InstanceUidAndIP();
            instance.setIp(ip);
            instanceUidAndIPList.add(instance);
        });
        Map<String, Object> map = Maps.newHashMap();
        map.put("status", status);
        map.put("endTime", System.currentTimeMillis() + chaosTask.getDuration());
        map.put("updateUser", chaosTask.getUpdateUser());
        map.put("instanceAndIps", instanceUidAndIPList);
        map.put("executedTimes", chaosTask.getExecutedTimes());
        int update = taskDao.update(chaosTask.getId(), map);
        log.info("podChaos update res: {}", update);
    }

    public String podKill(PodPO podPO, List<Pod> podList, ChaosTask chaosTask) {
        try {
            ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
            if (modeEnum.equals(ModeEnum.APPOINT)) {
                List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getPodPO()
                        .getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
                if (!ips.isEmpty()) {
                    return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
                }
            }
            switch (modeEnum) {
                case ANY -> deleteRandomPod(podList, chaosTask);
                case ALL -> deleteAllPods(podList, chaosTask);
                case APPOINT -> deleteAppointedPods(podList, chaosTask.getPodPO()
                        .getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList(), chaosTask);
                case null, default -> {
                    return "Invalid mode!";
                }
            }

            return chaosTask.getId().toString();
        } catch (Exception e) {
            log.error("pod kill error:{}", e);
            return "error";
        }
    }

    public String podFailure(PodPO podPO, List<Pod> podList, ChaosTask chaosTask) {
        try {
            ModeEnum modeEnum = ModeEnum.fromType(chaosTask.getModeType());
            if (modeEnum.equals(ModeEnum.APPOINT)) {
                List<String> ips = CommonUtil.getExcludedIPs(podList, chaosTask.getPodPO()
                        .getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).toList());
                if (!ips.isEmpty()) {
                    return "流水线不存在ip:" + String.join(",", ips) + "，请重新设置";
                }
            }
            switch (modeEnum) {
                case ANY -> failureRandomPod(podList, chaosTask);
                case ALL -> failureAllPods(podList, chaosTask);
                case APPOINT -> failureAppointedPods(podList, chaosTask.getPodPO().getInstanceUidAndIPList()
                        .stream().map(InstanceUidAndIP::getIp).toList(), chaosTask);
                case null, default -> {
                    return "Invalid mode!";
                }
            }
            // 时间轮终止任务
            wheelTimer(chaosTask);
            return chaosTask.getId().toString();
        } catch (Exception e) {
            log.error("pod failure error:{}", e);
            return "error";
        }
    }

    private void FailureRecover(Pod pod, ChaosTask chaosTask) {
        log.info("begin FailureRecover ns :{},name :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName());
        //替换container镜像
        // 只执行给定容器
        String containId = CommonUtil.getContainerIdByName(pod, chaosTask.getContainerName(),
                chaosTask.getProjectId(), chaosTask.getPipelineId());
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        List<Container> containers = pod.getSpec().getContainers();
        String needContainerName = containerStatuses.stream().filter(container -> container.getContainerID()
                .equals(containId)).map(ContainerStatus::getName).findFirst().get();
        String podName = pod.getMetadata().getName();
        containers.forEach(container -> {
            if (needContainerName.equals(container.getName())) {
                String containerName = container.getName();
                String annotationRecord = AnnotationUtil.GenKeyForImage(podName, containerName, false);
                log.info("FailureRecover.GenKeyForImage: {},isInit:{}", annotationRecord, false);
                if (pod.getMetadata().getAnnotations() == null) {
                    pod.getMetadata().setAnnotations(new HashMap<String, String>());
                }
                //判断是否有特殊标记
                if (pod.getMetadata().getAnnotations().containsKey(annotationRecord)) {
                    String originImage = pod.getMetadata().getAnnotations().get(annotationRecord);
                    container.setImage(originImage);
                    pod.getMetadata().getAnnotations().remove(annotationRecord);
                } else {
                    log.error("FailureRecover.fail not valid annotationRecord ,correct is :{},podName :{} containerName :{},is init:{}"
                            , annotationRecord, podName, containerName, false);
                }
            }
        });
        pod.getSpec().setContainers(containers);

        //替换initContainer镜像
        List<Container> initContainers = pod.getSpec().getInitContainers();
        initContainers.forEach(container -> {
            if (needContainerName.equals(container.getName())) {
                String containerName = container.getName();
                String annotationRecord = AnnotationUtil.GenKeyForImage(podName, containerName, true);
                log.info("FailurePod.GenKeyForImage: {},isInit:{}", annotationRecord, true);
                if (pod.getMetadata().getAnnotations() == null) {
                    pod.getMetadata().setAnnotations(new HashMap<String, String>());
                }
                //判断是否有特殊标记
                if (pod.getMetadata().getAnnotations().containsKey(annotationRecord)) {
                    String originImage = pod.getMetadata().getAnnotations().get(annotationRecord);
                    container.setImage(originImage);
                    pod.getMetadata().getAnnotations().remove(annotationRecord);
                } else {
                    log.error("FailureRecover.fail not valid annotationRecord ,correct is :{},podName :{} containerName :{},is init:{}"
                            , annotationRecord, podName, containerName, true);
                }
            }
        });
        pod.getSpec().setInitContainers(initContainers);
        //写回
        podClient.inNamespace(pod.getMetadata().getNamespace()).replace(pod);
    }

    private void failureRandomPod(List<Pod> podList, ChaosTask chaosTask) {
        Random random = new Random(System.currentTimeMillis());
        Pod pod = podList.get(random.nextInt(podList.size()));
        // 只执行给定容器
        String containId = CommonUtil.getContainerIdByName(pod, chaosTask.getContainerName(),
                chaosTask.getProjectId(), chaosTask.getPipelineId());
        FailurePod(pod, containId);
        updateStatus(List.of(pod.getStatus().getPodIP()), chaosTask, StatusEnum.actioning.type());
    }

    private void FailurePod(Pod pod, String containerId) {
        log.info("begin FailurePod ns :{},name :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName());

        //替换container镜像
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        List<Container> containers = pod.getSpec().getContainers();
        String needContainerName = containerStatuses.stream().filter(container -> container.getContainerID()
                .equals(containerId)).map(ContainerStatus::getName).findFirst().get();
        containers.forEach(container -> {
            if (container.getName().equals(needContainerName)) {
                String originImage = container.getImage();
                String containerName = container.getName();
                String annotationRecord = AnnotationUtil.GenKeyForImage(originImage, containerName, false);
                log.info("FailurePod.GenKeyForImage: {},isInit:{}", annotationRecord, false);
                if (pod.getMetadata().getAnnotations() == null) {
                    pod.getMetadata().setAnnotations(new HashMap<String, String>());
                }
                //如果注释有了，跳过此容器
                Map<String, String> annotations = pod.getMetadata().getAnnotations();
                if (annotations.containsKey(annotationRecord)) {
                    return;
                }
                //写入注释与pause镜像
                annotations.put(annotationRecord, originImage);
                container.setImage(CmdConstant.POD_FAILURE_PAUSE_IMAGE);
            }
        });
        pod.getSpec().setContainers(containers);

        //替换initContainers镜像
        List<Container> initContainers = pod.getSpec().getInitContainers();
        String podName = pod.getMetadata().getName();
        initContainers.forEach(container -> {
            if (container.getName().equals(needContainerName)) {
                String originImage = container.getImage();
                String containerName = container.getName();
                String annotationRecord = AnnotationUtil.GenKeyForImage(podName, containerName, true);
                log.info("FailurePod.GenKeyForImage: {},isInit:{}", annotationRecord, true);
                if (pod.getMetadata().getAnnotations() == null) {
                    pod.getMetadata().setAnnotations(new HashMap<String, String>());
                }
                //如果注释有了，跳过此容器
                Map<String, String> annotations = pod.getMetadata().getAnnotations();
                if (annotations.containsKey(annotationRecord)) {
                    return;
                }
                //写入注释与pause镜像
                annotations.put(annotationRecord, originImage);
                container.setImage(CmdConstant.POD_FAILURE_PAUSE_IMAGE);
            }
        });
        pod.getSpec().setInitContainers(initContainers);
        //写回
        podClient.inNamespace(pod.getMetadata().getNamespace()).replace(pod);
    }

    private void failureAllPods(List<Pod> podList, ChaosTask chaosTask) {
        // 只执行给定容器
        podList.forEach(pod -> {
            String containId = CommonUtil.getContainerIdByName(pod, chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            FailurePod(pod, containId);
        });
        updateStatus(podList.stream().map(it -> it.getStatus().getPodIP()).toList(), chaosTask, StatusEnum.actioning.type());
    }

    private void failureAppointedPods(List<Pod> podList, List<String> podIpList, ChaosTask chaosTask) {
        if (podIpList == null || podIpList.isEmpty()) {
            throw new IllegalArgumentException("podIpList is empty");
        }

        podList.forEach(pod -> {
            if (podIpList.contains(pod.getStatus().getPodIP())) {
                String containId = CommonUtil.getContainerIdByName(pod, chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                FailurePod(pod, containId);
            }
        });
        updateStatus(podIpList, chaosTask, StatusEnum.actioning.type());
    }

    private void deleteRandomPod(List<Pod> podList, ChaosTask chaosTask) {
        Random random = new Random(System.currentTimeMillis());
        Pod pod = podList.get(random.nextInt(podList.size()));
        podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
        updateStatus(List.of(pod.getStatus().getPodIP()), chaosTask, StatusEnum.actioning.type());
    }

    private void deleteAllPods(List<Pod> podList, ChaosTask chaosTask) {
        podList.forEach(pod -> {
            podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
        });
        updateStatus(podList.stream().map(it -> it.getStatus().getPodIP()).toList(), chaosTask, StatusEnum.actioning.type());
    }

    private void deleteAppointedPods(List<Pod> podList, List<String> podIpList, ChaosTask chaosTask) {
        if (podIpList == null || podIpList.isEmpty()) {
            throw new IllegalArgumentException("podIpList is empty");
        }

        podList.forEach(pod -> {
            if (podIpList.contains(pod.getStatus().getPodIP())) {
                podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
            }
        });
        updateStatus(podIpList, chaosTask, StatusEnum.actioning.type());
    }

    private void killRandomContainers(List<GrpcPodAndChannel> grpcPodAndChannels, ChaosTask chaosTask) {

        Random random = new Random(System.currentTimeMillis());
        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        String containId = CommonUtil.getContainerIdByName(grpcPodAndChannel.getPod(), chaosTask.getContainerName(),
                chaosTask.getProjectId(), chaosTask.getPipelineId());

        killContainer(grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel(), containId);
        updateStatus(List.of(grpcPodAndChannel.getPod().getStatus().getPodIP()), chaosTask, StatusEnum.actioning.type());
    }

    private void killAllContainers(List<Pod> podList, List<GrpcPodAndChannel> grpcPodAndChannels, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                    chaosTask.getProjectId(), chaosTask.getPipelineId());
            killContainer(podAndChannel.getPod(), podAndChannel.getChannel(), containId);
        });
        updateStatus(grpcPodAndChannels.stream().map(it -> it.getPod().getStatus().getPodIP()).toList(), chaosTask, StatusEnum.actioning.type());
    }

    private void killAppointedContainers(List<String> ips, List<GrpcPodAndChannel> grpcPodAndChannels, ChaosTask chaosTask) {
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (ips.contains(podAndChannel.getPod().getStatus().getPodIP())) {
                String containId = CommonUtil.getContainerIdByName(podAndChannel.getPod(), chaosTask.getContainerName(),
                        chaosTask.getProjectId(), chaosTask.getPipelineId());
                killContainer(podAndChannel.getPod(), podAndChannel.getChannel(), containId);
            }
        });
        updateStatus(ips, chaosTask, StatusEnum.actioning.type());
    }

    private void killContainer(Pod pod, ManagedChannel channel, String containId) {
        log.info("begin killContainer name:{}", pod.getMetadata().getName());
        Chaosdaemon.ContainerRequest containerRequest = Chaosdaemon.ContainerRequest.newBuilder()
                .setContainerId(containId).setAction(Chaosdaemon.ContainerAction.newBuilder()
                        .setAction(Chaosdaemon.ContainerAction.Action.forNumber(0))).build();
        Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).containerKill(containerRequest);
        log.info("killContainer req :{},res :{}", containerRequest, empty);
    }

}
