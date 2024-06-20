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
import net.sf.cglib.beans.BeanCopier;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.StressBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.StressPO;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StressChaosService {

    @Value("$grpc.port")
    private String port;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private ChaosTaskDao taskDao;

    public String createStressTask(StressBO stressBO) {
        ChaosTask chaosTask = ChaosTask.of(stressBO, TaskEnum.stress.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), stressBO.getExperimentName());
        StressPO stressPO = new StressPO();
        BeanCopier beanCopier = net.sf.cglib.beans.BeanCopier.create(StressBO.class, StressPO.class, false);
        beanCopier.copy(stressBO, stressPO, null);
        chaosTask.setStressPO(stressPO);
        Object id = taskDao.insert(chaosTask);

        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", stressBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            //todo 多容器需要根据image过滤
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            String hostIP = podList.get(0).getStatus().getHostIP();
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            StringBuffer memoryStressors = new StringBuffer();
            if (stressBO.getVmBytes() != null && stressBO.getVmBytes() > 0) {
                memoryStressors.append(CmdConstant.MEM_STRESS_SIZE).append(stressBO.getVmBytes()).append(stressBO.getVmUnit()).append(CmdConstant.BLANK_SPACE);
            }
            if (stressBO.getVmTimeOut() != null && stressBO.getVmTimeOut() > 0) {
                memoryStressors.append(CmdConstant.MEM_STRESS_TIME).append(stressBO.getVmTimeOut()).append(stressBO.getVmTimeOutUnit()).append(CmdConstant.BLANK_SPACE);
            }
            if (!memoryStressors.isEmpty()) {
                memoryStressors.append(CmdConstant.MEM_STRESS_WORKER);
            }
            StringBuffer cpuStressors = new StringBuffer();
            if (stressBO.getCpuNum() != null && stressBO.getCpuNum() > 0) {
                cpuStressors.append(CmdConstant.CPU).append(stressBO.getCpuNum()).append(CmdConstant.BLANK_SPACE);
            }
            if (stressBO.getCpuTimeOut() != null && stressBO.getCpuTimeOut() > 0) {
                cpuStressors.append(CmdConstant.TIMEOUT).append(stressBO.getCpuTimeOut()).append(stressBO.getCpuTimeOutUnit()).append(CmdConstant.BLANK_SPACE);
            }
            Chaosdaemon.ExecStressRequest stressRequest = null;
            if (!memoryStressors.isEmpty() && cpuStressors.isEmpty()) {
                stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setMemoryStressors(memoryStressors.toString()).setTarget(containId).build();
            } else if (memoryStressors.isEmpty() && !cpuStressors.isEmpty()) {
                stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setCpuStressors(cpuStressors.toString()).setTarget(containId).build();
            } else if (!memoryStressors.isEmpty() && !cpuStressors.isEmpty()) {
                stressRequest = Chaosdaemon.ExecStressRequest.newBuilder().setEnterNS(true).setMemoryStressors(memoryStressors.toString()).setCpuStressors(cpuStressors.toString()).setTarget(containId).build();
            }
            Chaosdaemon.ExecStressResponse response = ChaosDaemonGrpc.newBlockingStub(channel).execStressors(stressRequest);
            Map<String, Object> map = new HashMap<>();
            map.put("stressPO.cpuInstance", response.getCpuInstance());
            map.put("stressPO.cpuInstanceUid", response.getCpuInstanceUid());
            map.put("stressPO.memoryInstance", response.getMemoryInstance());
            map.put("stressPO.memoryInstanceUid", response.getMemoryInstanceUid());
            taskDao.update(id, map);

        }
        return id.toString();
    }

    public void cancelStress(StressBO stressBO) {
        if (stressBO.getId() == null) {
            return;
        }

        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", stressBO.getPipelineId().toString()).list().getItems();
        if (podList != null) {
            String hostIP = podList.get(0).getStatus().getHostIP();
            ChaosTask chaosTask = taskDao.getById(new ObjectId(stressBO.getId()), ChaosTask.class);
            String memoryInstance = chaosTask.getStressPO().getMemoryInstance();
            String memoryInstanceUid = chaosTask.getStressPO().getMemoryInstanceUid();
            String cpuInstance = chaosTask.getStressPO().getCpuInstance();
            String cpuInstanceUid = chaosTask.getStressPO().getCpuInstanceUid();
            Chaosdaemon.CancelStressRequest request = null;
            if (StringUtils.isNotEmpty(memoryInstance) && StringUtils.isEmpty(cpuInstance)) {
                request = Chaosdaemon.CancelStressRequest.newBuilder().setMemoryInstance(memoryInstance).setMemoryInstanceUid(memoryInstanceUid).build();
            } else if (StringUtils.isEmpty(memoryInstance) && StringUtils.isNotEmpty(cpuInstance)) {
                request = Chaosdaemon.CancelStressRequest.newBuilder().setCpuInstance(cpuInstance).setCpuInstanceUid(cpuInstanceUid).build();
            } else if (StringUtils.isNotEmpty(memoryInstance) && StringUtils.isNotEmpty(cpuInstance)) {
                request = Chaosdaemon.CancelStressRequest.newBuilder().setMemoryInstance(memoryInstance).setMemoryInstanceUid(memoryInstanceUid).setCpuInstance(cpuInstance).setCpuInstanceUid(cpuInstanceUid).build();
            }

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(hostIP, Integer.valueOf(port))
                    .usePlaintext()
                    .build();
            try {
                ChaosDaemonGrpc.newBlockingStub(channel).cancelStressors(request);
            } catch (Exception e) {

            } finally {
                long currentTimeMillis = System.currentTimeMillis();
                Map<String, Object> map = new HashMap<>();
                map.put("updateTime", currentTimeMillis);
                map.put("status", StatusEnum.recovered.type());
                taskDao.update(chaosTask.getId(), map);
            }
            log.info("");
        }
    }
}
