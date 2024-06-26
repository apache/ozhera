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
package run.mone.chaos.operator.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-01-04 15:56
 */
@Data
public class ChaosTaskDetailVO implements Serializable {

    private String id;

    private String experimentName;

    private Integer pipelineId;

    private Integer projectId;

    private String projectName;

    private Integer modeType;

    private List<String> podIpList;

    private Long duration;

    private Integer taskType;

    private Integer status;

    private String containerName;

    private String operateParam;

    private String createUser;

    private Integer executedTimes;

    private Long startTime;

    private String executor;

    private Integer containerNum;

    public ChaosTaskDetailVO(ChaosTask chaosTask) {
        id = chaosTask.getId().toString();
        experimentName = chaosTask.getExperimentName();
        pipelineId = chaosTask.getPipelineId();
        projectId = chaosTask.getProjectId();
        projectName = chaosTask.getProjectName();
        modeType = chaosTask.getModeType();
        duration = chaosTask.getDuration();
        taskType = chaosTask.getTaskType();
        status = chaosTask.getStatus();
        containerName = chaosTask.getContainerName();
        podIpList = chaosTask.getPodIpList();
        executedTimes = chaosTask.getExecutedTimes();
        containerNum = chaosTask.getContainerNum();
    }

    public ChaosTaskDetailVO() {

    }

}
