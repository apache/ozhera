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
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.chaosBot.CreateBotCronBo;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-11 14:39
 */
@Slf4j
public abstract class TaskBaseService {


    public Gson gson = new Gson();

    public abstract String save(CreateChaosTaskBo createChaosTaskBo, PipelineBO pipelineBO, Integer taskStatus);

    public abstract String execute(ChaosTask chaosTask);

    public abstract String recover(ChaosTask chaosTask);

    public String wheelTimer(ChaosTask chaosTask) {
        TaskWheelTimer taskWheelTimer = Ioc.ins().getBean(TaskWheelTimer.class);
        taskWheelTimer.newTimeout(() -> {
            ChaosTaskService chaosTaskService = Ioc.ins().getBean(ChaosTaskService.class);
            ChaosTask chaosTaskById = chaosTaskService.getChaosTaskById(chaosTask.getId().toString());
            try {
                if (chaosTaskById == null) {
                    log.warn("chaos is null , id:{}", chaosTask.getId().toString());
                    return;
                }
                if (chaosTaskById.getStatus() != StatusEnum.actioning.type()) {
                    log.warn("chaos id:{}, status:{}", chaosTask.getId().toString(), chaosTaskById.getStatus());
                    return;
                }
                log.warn("taskWheelTimer kill task, id:{}", chaosTask.getId().toString());

                boolean chaosRecoverLock = ((LockService) Ioc.ins().getBean(LockService.class)).chaosRecoverLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
                if (chaosRecoverLock) {
                    recover(chaosTaskById);
                    ((LockService) Ioc.ins().getBean(LockService.class)).chaosRecoverUnLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
                    ((LockService) Ioc.ins().getBean(LockService.class)).chaosUnLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
                } else {
                    log.info("taskWheelTimer kill task, id:{}, lock fail", chaosTask.getId().toString());
                }
            } catch (Exception e) {
                log.error("stress task taskWheelTimer error, taskId:{}", chaosTask.getId().toString(), e);
            }
        }, chaosTask.getDuration());
        return null;
    }

    public List<GrpcPodAndChannel> getSelectedChannels(ChaosTask chaosTask, List<GrpcPodAndChannel> grpcPodAndChannels, Random random) {
        Set<Integer> usedIndices = new HashSet<>();
        List<GrpcPodAndChannel> selectedChannels = new ArrayList<>();

        if (ObjectUtils.isEmpty(chaosTask.getContainerNum()) || chaosTask.getContainerNum() > grpcPodAndChannels.size()) {
            chaosTask.setContainerNum(1);
        }

        while (selectedChannels.size() < chaosTask.getContainerNum() && usedIndices.size() < grpcPodAndChannels.size()) {
            int randomIndex = random.nextInt(grpcPodAndChannels.size());
            if (usedIndices.add(randomIndex)) {
                GrpcPodAndChannel grpcPodAndChannel = grpcPodAndChannels.get(randomIndex);
                selectedChannels.add(grpcPodAndChannel);
            }
        }
        return selectedChannels;
    }


    public List<Pod> getPods(ChaosTask chaosTask, List<Pod> podList) {
        Set<Integer> usedIndices = new HashSet<>();
        List<Pod> selectedPods = new ArrayList<>();
        if (ObjectUtils.isEmpty(chaosTask.getContainerNum()) || chaosTask.getContainerNum() > podList.size()) {
            chaosTask.setContainerNum(1);
        }
        while (selectedPods.size() < chaosTask.getContainerNum()) {
            Random random = new Random();
            int index = random.nextInt(podList.size());
            if (usedIndices.add(index)) {
                selectedPods.add(podList.get(index));
            }
        }
        return selectedPods;
    }

}
