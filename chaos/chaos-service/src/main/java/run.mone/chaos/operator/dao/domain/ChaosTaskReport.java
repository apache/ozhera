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

import dev.morphia.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.TaskEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-16 10:51
 */
@Data
@Entity("chaosTaskReport")
@Indexes({
        @Index(fields = @Field("projectId")),
        @Index(fields = @Field("projectName")),
        @Index(fields = @Field("pipelineId")),
        @Index(fields = @Field("taskType")),
        @Index(fields = @Field("experimentName")),
        @Index(fields = @Field("experimentId"))
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChaosTaskReport extends BaseDomain implements Serializable {

    private String reportName;

    private Integer projectId;

    private String projectName;

    private Integer pipelineId;

    private String experimentId;

    private String experimentName;

    private Integer executedTimes;

    private Integer taskType;

    private Integer status;

    private String taskCreator;

    private String snapshot;

    private String monitorUrl;

    public static ChaosTaskReport of(ChaosTask chaosTask) {
        String reportName = String.format("%s-%s-%s-%s", chaosTask.getProjectName(), TaskEnum.fromType(chaosTask.getTaskType()), ModeEnum.fromType(chaosTask.getModeType()).typeName(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        ChaosTaskReport report = ChaosTaskReport.builder()
                .pipelineId(chaosTask.getPipelineId())
                .reportName(reportName)
                .projectId(chaosTask.getProjectId())
                .projectName(chaosTask.getProjectName())
                .executedTimes(chaosTask.getExecutedTimes())
                .experimentName(chaosTask.getExperimentName())
                .experimentId(chaosTask.getId().toString())
                .taskType(chaosTask.getTaskType())
                .status(chaosTask.getStatus())
                .taskCreator(chaosTask.getCreateUser())
                .monitorUrl("")
                .build();
        report.setCreateTime(System.currentTimeMillis());
        report.setCreateUser(chaosTask.getCreateUser());
        return report;
    }


}
