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

import com.google.protobuf.Empty;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.PodBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;

import javax.annotation.Resource;
import java.util.*;

import com.google.gson.Gson;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;
import run.mone.chaos.operator.util.AnnotationUtil;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/5 11:28
 */

@Slf4j
@Service
public class PodChaosService {

    @Value("${chaos.daemon.url}")
    private String daemonUrl;

    @Value("${chaos.daemon.port}")
    private String daemonPort;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private ChaosTaskDao taskDao;

    @Resource
    private GrpcChannelService grpcChannelService;

    private final static Gson gson = new Gson();

    public String containerKill(PodBO podBo) {
        ChaosTask chaosTask = ChaosTask.of(podBo, TaskEnum.pod.type(), podBo.getMode(), StatusEnum.un_action.type(), podBo.getExperimentName());
        try {
           /* ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("10.38.201.28", Integer.parseInt("31767"))
                    .usePlaintext()
                    .build();*/

            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", podBo.getPipelineId().toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }

            List<GrpcPodAndChannel> grpcPodAndChannels = grpcChannelService.grpcPodAndChannelList(podList);
            if (grpcPodAndChannels.isEmpty()) {
                return "unable to connect any grpc server!";
            }

            taskDao.insert(chaosTask);
            ModeEnum modeEnum = ModeEnum.fromType(podBo.getMode());
            switch (modeEnum) {
                case ANY:
                    killRandomContainers(grpcPodAndChannels);
                    break;
                case ALL:
                    killAllContainers(podList, grpcPodAndChannels);
                    break;
                case APPOINT:
                    if (podBo.getPodIpList() == null || podBo.getPodIpList().isEmpty()) {
                        return "podIpList is empty";
                    }
                    List<String> ips = podBo.getPodIpList();
                    killAppointedContainers(ips, grpcPodAndChannels);
                    break;
                case null, default:
                    return "Invalid mode!";
            }

            return "ok";
        } catch (Exception e) {
            log.error("container kill error:{}", e);
            return "error";
        } finally {
            if (chaosTask.getId() != null) {
                // k8s会自动拉起，无须恢复，状态置位recover
                taskDao.update(chaosTask.getId(), Collections.singletonMap("status", StatusEnum.recovered.type()));
            }
        }
    }

    private void killRandomContainers(List<GrpcPodAndChannel> grpcPodAndChannels) {
        Random random = new Random(System.currentTimeMillis());
        GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(random.nextInt(grpcPodAndChannels.size()));
        killContainer(grpcPodAndChannel.getPod(), grpcPodAndChannel.getChannel());
    }

    private void killAllContainers(List<Pod> podList, List<GrpcPodAndChannel> grpcPodAndChannels) {
        if (podList.size() != grpcPodAndChannels.size()) {
            log.error("podList size :{} is not equal to grpcPodAndChannels size:{}", podList.size(), grpcPodAndChannels.size());
            // TODO:入库记录
            return;
        }
        grpcPodAndChannels.forEach(podAndChannel -> killContainer(podAndChannel.getPod(), podAndChannel.getChannel()));
    }

    private void killAppointedContainers(List<String> ips, List<GrpcPodAndChannel> grpcPodAndChannels) {
        if (ips.size() != grpcPodAndChannels.size()) {
            log.error("ips size :{} is not equal to grpcPodAndChannels size:{}", ips.size(), grpcPodAndChannels.size());
            // TODO:入库记录
            return;
        }
        grpcPodAndChannels.forEach(podAndChannel -> {
            if (ips.contains(podAndChannel.getPod().getStatus().getPodIP())) {
                killContainer(podAndChannel.getPod(), podAndChannel.getChannel());
            }
        });
    }


    public String podKill(PodBO podBO) {
        ChaosTask chaosTask = ChaosTask.of(podBO, TaskEnum.pod.type(), podBO.getMode(), StatusEnum.un_action.type(), podBO.getExperimentName());
        Object insertRes = taskDao.insert(chaosTask);
        log.info("podKill access insert res:{}", insertRes);

        try {
            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", podBO.getPipelineId().toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }
            ModeEnum modeEnum = ModeEnum.fromType(podBO.getMode());
            switch (modeEnum) {
                case ANY:
                    deleteRandomPod(podList);
                    break;
                case ALL:
                    deleteAllPods(podList);
                    break;
                case APPOINT:
                    deleteAppointedPods(podList, podBO.getPodIpList());
                    break;
                case null, default:
                    return "Invalid mode!";
            }

            Map<String, Object> updateMap = new HashMap<>(1);
            // k8s自动会恢复，因此状态直接置位recover
            updateMap.put("status", StatusEnum.recovered.type());
            taskDao.update(chaosTask.getId(), updateMap);
            return "ok";
        } catch (Exception e) {
            log.error("pod kill error:{}", e);
            return "error";
        }
    }

    private void deleteRandomPod(List<Pod> podList) {
        Random random = new Random(System.currentTimeMillis());
        Pod pod = podList.get(random.nextInt(podList.size()));
        podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
    }

    private void deleteAllPods(List<Pod> podList) {
        podList.forEach(pod -> {
            podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
        });
    }

    private void deleteAppointedPods(List<Pod> podList, List<String> podIpList) {
        if (podIpList == null || podIpList.isEmpty()) {
            throw new IllegalArgumentException("podIpList is empty");
        }

        podList.forEach(pod -> {
            if (podIpList.contains(pod.getStatus().getPodIP())) {
                podClient.inNamespace(pod.getMetadata().getNamespace()).delete(pod);
            }
        });
    }

    public String podFailure(PodBO podBo) {
        ChaosTask chaosTask = ChaosTask.of(podBo, TaskEnum.pod.type(), podBo.getMode(), StatusEnum.un_action.type(), podBo.getExperimentName());
        Object insertRes = taskDao.insert(chaosTask);
        log.info("podKill access insert res:{}", insertRes);

        try {
            ChaosTask byId = taskDao.getById(insertRes, ChaosTask.class);
            log.info("podFailure.getById:{}", byId);

            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", podBo.getPipelineId().toString()).list().getItems();
            if (podList == null || podList.isEmpty()) {
                return "not found pod!";
            }
            ModeEnum modeEnum = ModeEnum.fromType(podBo.getMode());
            switch (modeEnum) {
                case ANY:
                    failureRandomPod(podList);
                    break;
                case ALL:
                    failureAllPods(podList);
                    break;
                case APPOINT:
                    failureAppointedPods(podList, podBo.getPodIpList());
                    break;
                case null, default:
                    return "Invalid mode!";
            }

            return insertRes.toString();
        } catch (Exception e) {
            log.error("pod failure error:{}", e);
            return "error";
        } finally {
            Map<String, Object> updateMap = new HashMap<>(1);
            updateMap.put("status", StatusEnum.actioning.type());
            taskDao.update(chaosTask.getId(), updateMap);
        }
    }

    private void failureRandomPod(List<Pod> podList) {
        Random random = new Random(System.currentTimeMillis());
        Pod pod = podList.get(random.nextInt(podList.size()));
        FailurePod(pod);
    }

    private void failureAllPods(List<Pod> podList) {
        podList.forEach(this::FailurePod);
    }

    private void failureAppointedPods(List<Pod> podList, List<String> podIpList) {
        if (podIpList == null || podIpList.isEmpty()) {
            throw new IllegalArgumentException("podIpList is empty");
        }

        podList.forEach(pod -> {
            if (podIpList.contains(pod.getStatus().getPodIP())) {
                FailurePod(pod);
            }
        });
    }

    public String podFailureRecover(PodBO podBo) {
        try {
            ChaosTask chaosTaskFromDb = taskDao.getById(podBo.getId(), ChaosTask.class);
            if (chaosTaskFromDb == null) {
                return "该pod未执行过此混沌实验,无需恢复!";
            }
            if (chaosTaskFromDb.getStatus() != StatusEnum.actioning.type()) {
                return "异常：实验状态不为actioned,请联系xxx!";
            }
            List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", podBo.getPipelineId().toString()).list().getItems();
            ModeEnum modeEnum = ModeEnum.fromType(podBo.getMode());
            switch (modeEnum) {
                case ANY:
                case ALL:
                    podList.forEach(this::FailureRecover);
                    break;
                case APPOINT:
                    if (podBo.getPodIpList() == null || podBo.getPodIpList().isEmpty()) {
                        return "podIpList is empty";
                    }
                    List<String> ips = podBo.getPodIpList();
                    podList.forEach(pod -> {
                        if (ips.contains(pod.getStatus().getPodIP())) {
                            FailureRecover(pod);
                        }
                    });
                    break;
                case null, default:
                    return "Invalid mode!";
            }
            return "ok";
        } catch (Exception e) {
            log.error("pod failure recover error:{}", e);
            return "error";
        } finally {
            Map<String, Object> updateMap = new HashMap<>(1);
            updateMap.put("status", StatusEnum.recovered.type());
            taskDao.update(podBo.getId(), updateMap);
            // TODO: 锁恢复
        }
    }

    private void killContainer(Pod pod, ManagedChannel channel) {
        log.info("begin killContainer name:{}", pod.getMetadata().getName());
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        containerStatuses.forEach(containerStatus -> {
            String containerID = containerStatus.getContainerID();
            Chaosdaemon.ContainerRequest containerRequest = Chaosdaemon.ContainerRequest.newBuilder()
                    .setContainerId(containerID).setAction(Chaosdaemon.ContainerAction.newBuilder()
                            .setAction(Chaosdaemon.ContainerAction.Action.forNumber(0))).build();
            Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).containerKill(containerRequest);
            log.info("killContainer req :{},res :{}", containerRequest, empty);
        });
    }

    private void FailurePod(Pod pod) {
        log.info("begin FailurePod ns :{},name :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName());

        //替换container镜像
        List<Container> containers = pod.getSpec().getContainers();
        containers.forEach(container -> {
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
        });
        pod.getSpec().setContainers(containers);

        //替换initContainers镜像
        List<Container> initContainers = pod.getSpec().getInitContainers();
        String podName = pod.getMetadata().getName();
        initContainers.forEach(container -> {
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
        });
        pod.getSpec().setInitContainers(initContainers);
        //写回
        podClient.inNamespace(pod.getMetadata().getNamespace()).replace(pod);
    }

    private void FailureRecover(Pod pod) {
        log.info("begin FailureRecover ns :{},name :{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName());
        //替换container镜像
        List<Container> containers = pod.getSpec().getContainers();
        String podName = pod.getMetadata().getName();
        containers.forEach(container -> {
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
        });
        pod.getSpec().setContainers(containers);

        //替换initContainer镜像
        List<Container> initContainers = pod.getSpec().getInitContainers();
        initContainers.forEach(container -> {
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
        });
        pod.getSpec().setInitContainers(initContainers);
        //写回
        podClient.inNamespace(pod.getMetadata().getNamespace()).replace(pod);
    }

}
