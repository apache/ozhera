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
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author caobaoyu
 * @description:
 * @date 2023-12-25 10:10
 */
@Data
@Entity("chaosTaskLog")
@Indexes({
        @Index(fields = @Field("projectId")),
        @Index(fields = @Field("projectName")),
        @Index(fields = @Field("pipelineId")),
        @Index(fields = @Field("taskType")),
        @Index(fields = @Field("experimentName"))
})
@EqualsAndHashCode(callSuper = false)
public class ChaosTaskLog extends BaseDomain implements Serializable {
    private String taskId;

    private Integer projectId;

    private String projectName;

    private Integer pipelineId;

    private Integer taskType;

    private String experimentName;

    private Integer modeType;

    private Integer status;

    private Integer executedTimes;

    private String operateParam;

    private List<InstanceUidAndIP> instanceUidAndIPList;

    public static ChaosTaskLog of(ChaosTask chaosTask) {
        ChaosTaskLog chaosTaskLog = new ChaosTaskLog();
        BeanUtils.copyProperties(chaosTask, chaosTaskLog);
        if (chaosTask.getId() != null) {
            chaosTaskLog.setTaskId(chaosTask.getId().toString());
        }
        chaosTaskLog.setId(null);
        chaosTaskLog.setInstanceUidAndIPList(chaosTask.getInstanceAndIps());
        chaosTaskLog.setCreateUser(chaosTask.getCreateUser());
        chaosTaskLog.setCreateTime(System.currentTimeMillis());
        chaosTaskLog.setUpdateUser(chaosTask.getUpdateUser());
        chaosTaskLog.setUpdateTime(System.currentTimeMillis());
        return chaosTaskLog;
    }

}
