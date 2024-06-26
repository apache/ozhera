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

import java.io.Serializable;

/**
 * @author caobaoyu
 * @description: 执行日志
 * @date 2024-04-23 10:02
 */
@Data
@Entity("taskExeLog")
@Indexes({
        @Index(fields = @Field("taskId"))
})
public class TaskExeLog extends BaseDomain implements Serializable {

    private String experimentName;

    private Long startTime;

    private String taskId;

    private String executor;

    private Integer experimentTimes;

    private Long duration;

}
