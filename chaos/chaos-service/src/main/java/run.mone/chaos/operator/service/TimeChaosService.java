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
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.TimeBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.TimePO;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author caobaoyu
 * @description: 时钟偏移混沌实验
 * @date 2024-01-03 15:05
 */
@Service
@Slf4j
public class TimeChaosService {

    @Resource
    private ChaosTaskDao taskDao;

    @Value("$grpc.port")
    private String port;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    public String setTimeOffset(TimeBO timeBO) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", timeBO.getPipelineId().toString()).list().getItems();
        if (CollectionUtils.isNotEmpty(podList)) {
            ChaosTask chaosTask = ChaosTask.of(timeBO, TaskEnum.time.type(), ModeEnum.APPOINT.type(), StatusEnum.un_action.type(), timeBO.getExperimentName());
            String containId = podList.get(0).getStatus().getContainerStatuses().get(0).getContainerID();
            TimePO timePO = new TimePO();
            timePO.setTimeOffset(timeBO.getTimeOffset());
            timePO.setInstanceId(containId);
            chaosTask.setTimePO(timePO);
            Object taskId = taskDao.insert(chaosTask);

            //todo 多容器需要根据image过滤
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("10.38.201.28", Integer.parseInt("31767"))
                    .usePlaintext()
                    .build();

            Chaosdaemon.TimeRequest request = Chaosdaemon.TimeRequest.newBuilder()
                    .setContainerId(containId)
                    .setClkIdsMask(1L)
                    .setUid(taskId.toString())
                    .setSec(timeBO.getTimeOffset())
                    .build();
            try {
                com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).setTimeOffset(request);
                System.out.println(empty);
                Map<String, Object> map = new HashMap<>();
                timePO.setInstanceId(containId);
                timePO.setUid(taskId.toString());
                map.put("status", StatusEnum.actioning.type());
                map.put("timePO", timePO);
                taskDao.update(taskId, map);
                return taskId.toString();
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return "setTimeOffset error";
    }

    public String recoverTimeOffset(TimeBO timeBO) {
        ChaosTask chaosTaskFromDb = taskDao.getById(timeBO.getId(), ChaosTask.class);
        if (Objects.nonNull(chaosTaskFromDb) && Objects.nonNull(chaosTaskFromDb.getTimePO())) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("10.38.201.28", Integer.parseInt("31767"))
                    .usePlaintext()
                    .build();
            TimePO timePO = chaosTaskFromDb.getTimePO();
            Chaosdaemon.TimeRequest request = Chaosdaemon.TimeRequest.newBuilder()
                    .setContainerId(timePO.getInstanceId())
                    .setUid(timePO.getUid())
                    .build();
            try {
                com.google.protobuf.Empty empty = ChaosDaemonGrpc.newBlockingStub(channel).recoverTimeOffset(request);
                System.out.println(empty);
                Map<String, Object> map = new HashMap<>();
                map.put("status", StatusEnum.recovered.type());
                taskDao.update(new ObjectId(timeBO.getId()), map);
            } catch (Exception e) {
                log.error("", e);
                return "recover error";
            }

        }
        return "success";
    }


}
