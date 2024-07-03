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
package run.mone.chaos.operator.bo;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import run.mone.chaos.operator.constant.ModeEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author caobaoyu
 * @description: 用于创建任务
 * @date 2024-04-11 10:59
 */
@Data
public class CreateChaosTaskBo extends BaseBO implements Serializable {

    private String id;

    private String experimentName;

    private Integer pipelineId;

    private Integer projectId;

    private String projectName;

    private Integer mode;

    private Integer containerNum;

    private List<String> podIpList;

    private Integer executedTimes;

    /**
     * 运行时长
     */
    private Long duration;

    private Integer taskType;

    private String containerName;

    private String operateParam;

    private String createUser;

    @Override
    public Optional<String> paramValidate() {
        if (StringUtils.isBlank(experimentName)) {
            return Optional.of("experimentName is required");
        }
        if (Objects.isNull(pipelineId)) {
            return Optional.of("pipelineId is required");
        }
        if (StringUtils.isBlank(projectName)) {
            return Optional.of("projectName is required");
        }
        // 指定ip的情况下，ip不能为空
        if (null != ModeEnum.fromType(mode) && Objects.equals(ModeEnum.fromType(mode), ModeEnum.APPOINT) && CollectionUtils.isEmpty(podIpList)) {
            return Optional.of("podIpList is required");
        }

        // 随机模式下，容器数量如果为空，则默认设置为1
        if (ModeEnum.ANY.equals(ModeEnum.fromType(mode)) && ObjectUtils.isEmpty(containerNum)) {
            containerNum = 1;
        }

        if (ObjectUtils.isEmpty(taskType)) {
            return Optional.of("taskType is required");
        }
        if (StringUtils.isBlank(operateParam)) {
            return Optional.of("operateParam is required");
        }
        if (ObjectUtils.isEmpty(duration)) {
            return Optional.of("duration is required");
        }
        if (ObjectUtils.isEmpty(containerName)) {
            return Optional.of("container is required");
        }

        return super.paramValidate();
    }
}
