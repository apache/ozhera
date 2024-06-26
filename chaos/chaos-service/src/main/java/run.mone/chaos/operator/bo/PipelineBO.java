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
 * @author zhangping17
 */
@Data
public class PipelineBO extends BaseBO implements Serializable {

    private String experimentName;

    private Integer pipelineId;

    private Integer projectId;

    private String projectName;

    private Integer mode;

    private List<String> podIpList;

    /**
     * 运行时长
     */
    private Long duration;

    @Override
    public Optional<String> paramValidate() {
        if (StringUtils.isBlank(experimentName)) {
            return Optional.of("experimentName is required");
        }
        if (Objects.isNull(pipelineId)) {
            return Optional.of("pipelineId is required");
        }

        if (null != ModeEnum.fromType(mode) && CollectionUtils.isEmpty(podIpList)) {
            return Optional.of("podIpList is required");
        }

        if (ObjectUtils.isEmpty(duration)) {
            return Optional.of("duration is required");
        }

        return super.paramValidate();
    }
}
