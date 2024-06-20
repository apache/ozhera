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
package run.mone.chaos.operator.dao.domain;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity("chaosTask")
@Indexes({
        @Index(fields = @Field("projectId")),
        @Index(fields = @Field("projectName")),
        @Index(fields = @Field("pipelineId")),
        @Index(fields = @Field("taskType")),
        @Index(fields = @Field("experimentName"))
})
public class ChaosTask extends BaseDomain implements Serializable {

    private Integer projectId;

    private String projectName;

    private Integer pipelineId;

    private Integer taskType;

    private String experimentName;

    private Integer containerNum;

    private Integer modeType;

    private Long duration;

    /**
     * 执行次数
     */
    private Integer executedTimes;

    private List<String> podIpList;

    private List<InstanceUidAndIP> instanceAndIps = new ArrayList<>();

    private Integer status;

    private String containerName;

    private StressPO stressPO;

    private NetworkPO networkPO;

    private HttpPO httpPO;

    private IOPO ioPO;

    private JvmPO jvmPO;

    private TimePO timePO;

    private PodPO podPO;

    public static ChaosTask of(PipelineBO pipelineBO, int taskType, int modeType, int statusType, String experimentName) {
        ChaosTask chaosTask = new ChaosTask();
        chaosTask.setProjectId(pipelineBO.getProjectId());
        chaosTask.setProjectName(pipelineBO.getProjectName());
        chaosTask.setPipelineId(pipelineBO.getPipelineId());
        chaosTask.setDuration(pipelineBO.getDuration());
        chaosTask.setTaskType(taskType);
        chaosTask.setModeType(modeType);
        chaosTask.setStatus(statusType);
        chaosTask.setExperimentName(experimentName);
        return chaosTask;
    }

    public static ChaosTask of(CreateChaosTaskBo taskBo, int statusType) {
        ChaosTask chaosTask = new ChaosTask();
        if (StringUtils.isNotBlank(taskBo.getId())) {
            chaosTask.setId(new ObjectId(taskBo.getId()));
        }
        chaosTask.setProjectId(taskBo.getProjectId());
        chaosTask.setProjectName(taskBo.getProjectName());
        chaosTask.setPipelineId(taskBo.getPipelineId());
        chaosTask.setDuration(taskBo.getDuration());
        chaosTask.setExperimentName(taskBo.getExperimentName());
        chaosTask.setCreateUser(taskBo.getCreateUser());
        chaosTask.setTaskType(taskBo.getTaskType());
        chaosTask.setModeType(taskBo.getMode());
        chaosTask.setContainerNum(Optional.ofNullable(taskBo.getContainerNum()).orElse(1));
        chaosTask.setExecutedTimes(ObjectUtils.isEmpty(taskBo.getExecutedTimes()) ? 0 : taskBo.getExecutedTimes());
        chaosTask.setStatus(statusType);
        chaosTask.setPodIpList(taskBo.getPodIpList());
        chaosTask.setContainerName(taskBo.getContainerName());
        chaosTask.setCreateUser(taskBo.getCreateUser());
        chaosTask.setUpdateUser(taskBo.getCreateUser());
        return chaosTask;
    }

}
