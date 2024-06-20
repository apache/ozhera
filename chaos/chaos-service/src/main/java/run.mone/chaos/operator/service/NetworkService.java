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

import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.NetworkBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.NetworkPO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class NetworkService {

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private ChaosTaskDao taskDao;

    @Value("$grpc.port")
    private String port;

    public void addDelay(NetworkBO networkBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", networkBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            ChaosTask chaosTask = ChaosTask.of(networkBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), networkBO.getExperimentName());
            NetworkPO networkPO = new NetworkPO();
            networkPO.setDelayTime(networkBO.getDelayTime());
            chaosTask.setNetworkPO(networkPO);
            taskDao.insert(chaosTask);

            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().setTime(networkBO.getDelayTime() * 1000).build();
            Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
            Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();

            try {
                com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTcs(tcsRequest);
                System.out.println(empty);
            } catch (Exception e) {
                log.error("", e);
            }

        }
    }

    public void addLoss(NetworkBO networkBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", networkBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            ChaosTask chaosTask = ChaosTask.of(networkBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), networkBO.getExperimentName());
            NetworkPO networkPO = new NetworkPO();
            networkPO.setDelayTime(networkBO.getLossPercent());
            chaosTask.setNetworkPO(networkPO);
            taskDao.insert(chaosTask);

            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().setLoss(networkBO.getLossPercent()).build();
            Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
            Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();

            com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTcs(tcsRequest);
            System.out.println(empty);
        }
    }

    public void addCorruption(NetworkBO networkBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", networkBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            ChaosTask chaosTask = ChaosTask.of(networkBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), networkBO.getExperimentName());
            NetworkPO networkPO = new NetworkPO();
            networkPO.setDelayTime(networkBO.getCorruptionPercent());
            chaosTask.setNetworkPO(networkPO);
            taskDao.insert(chaosTask);

            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().setCorrupt(networkBO.getCorruptionPercent()).build();
            Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
            Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();

            com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTcs(tcsRequest);
            System.out.println(empty);
        }
    }

    public void addDuplicates(NetworkBO networkBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", networkBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            ChaosTask chaosTask = ChaosTask.of(networkBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), networkBO.getExperimentName());
            NetworkPO networkPO = new NetworkPO();
            networkPO.setDelayTime(networkBO.getDuplicatesPercent());
            chaosTask.setNetworkPO(networkPO);
            taskDao.insert(chaosTask);

            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().setDuplicate(networkBO.getDuplicatesPercent()).build();
            Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
            Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();

            com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTcs(tcsRequest);
            System.out.println(empty);
        }
    }

    public void clear(NetworkBO networkBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", networkBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    //.forAddress(hostIP, Integer.valueOf(port))
                    .forTarget("dns:///" + hostIP + ":" + port)
                    .usePlaintext()
                    .build();
            Chaosdaemon.Netem netem = Chaosdaemon.Netem.newBuilder().clearTime().build();
            Chaosdaemon.Tc tc = Chaosdaemon.Tc.newBuilder().setDevice("eth0").setType(Chaosdaemon.Tc.Type.NETEM).setNetem(netem).build();
            Chaosdaemon.TcsRequest tcsRequest = Chaosdaemon.TcsRequest.newBuilder().setEnterNS(true).setContainerId(containId).addTcs(tc).build();
            try {
                com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTcs(tcsRequest);
                System.out.println(empty);
            } catch (Exception e) {
                log.error("NetworkService clear error:", e);
            } finally {
                Map<String, Object> map = new HashMap<>();
                map.put("pipelineId", networkBO.getPipelineId());
                map.put("experimentName", networkBO.getExperimentName());
                map.put("taskType", TaskEnum.network.type());
                ChaosTask task = taskDao.getLatestByKvMap(map);
                if (!Objects.isNull(task)) {
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("status", StatusEnum.recovered.type());
                    taskDao.update(task.getId(), updateMap);
                }
            }
        }
    }
}
