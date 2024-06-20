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
import org.bson.types.ObjectId;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.JvmBO;
import run.mone.chaos.operator.constant.*;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.JvmPO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JvmService {

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private ChaosTaskDao taskDao;

    @Value("$grpc.port")
    private String port;

    public String injectJvmException(JvmBO jvmBO) {
        String doing = "Integer.valueOf(\"a\");";
        String rule = String.format(CmdConstant.JVM_TEMPLATE, UUID.randomUUID().toString(), jvmBO.getClName(), jvmBO.getMethodName(), doing);
        ChaosTask chaosTask = ChaosTask.of(jvmBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), jvmBO.getExperimentName());
        JvmPO jvmPO = new JvmPO();
        jvmPO.setType(JvmActionEnum.exception.type());
        jvmPO.setRule(rule);
        chaosTask.setJvmPO(jvmPO);
        Object id = taskDao.insert(chaosTask);
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", jvmBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            Chaosdaemon.InstallJVMRulesRequest request = Chaosdaemon.InstallJVMRulesRequest.newBuilder().setEnterNS(true).setContainerId(containId).setPort(9288).setRule(rule).build();
            ChaosDaemonGrpc.newBlockingStub(channel).installJVMRules(request);
        }
        return id.toString();
    }

    public String injectJvmReturn(JvmBO jvmBO) {
        String doing = String.format(CmdConstant.JVM_TEMPLATE_RETURN, jvmBO.getResponse());
        String rule = String.format(CmdConstant.JVM_TEMPLATE, UUID.randomUUID().toString(), jvmBO.getClName(), jvmBO.getMethodName(), doing);
        ChaosTask chaosTask = ChaosTask.of(jvmBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), jvmBO.getExperimentName());
        JvmPO jvmPO = new JvmPO();
        jvmPO.setType(JvmActionEnum.re.type());
        jvmPO.setRule(rule);
        chaosTask.setJvmPO(jvmPO);
        Object id = taskDao.insert(chaosTask);
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", jvmBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();

            Chaosdaemon.InstallJVMRulesRequest request = Chaosdaemon.InstallJVMRulesRequest.newBuilder().setEnterNS(true).setContainerId(containId).setPort(9288).setRule(rule).build();
            ChaosDaemonGrpc.newBlockingStub(channel).installJVMRules(request);
        }
        return id.toString();
    }

    public void unInjectJvm(JvmBO jvmBO) {
        ChaosTask chaosTask = taskDao.getById(new ObjectId(jvmBO.getId()), ChaosTask.class);

        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", jvmBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();

            Chaosdaemon.UninstallJVMRulesRequest request = Chaosdaemon.UninstallJVMRulesRequest.newBuilder().setEnterNS(true).setContainerId(containId).setPort(9288).setRule(chaosTask.getJvmPO().getRule()).build();
            ChaosDaemonGrpc.newBlockingStub(channel).uninstallJVMRules(request);
            System.out.println();
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusEnum.recovered.type());
            taskDao.update(new ObjectId(jvmBO.getId()), map);
        }
    }
}
