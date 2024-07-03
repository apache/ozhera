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

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-16 10:51
 */
@Data
public class ChaosTaskReportVO implements Serializable {

    private String id;

    private String reportName;

    private Integer projectId;

    private String projectName;

    private Integer pipelineId;

    private String experimentId;

    private String experimentName;

    private Integer taskType;

    private Integer status;

    private String taskCreator;

    private Long createTime;

    /**
     * 执行时间
     */
    private Long executeTime;

    private Integer executedTimes;

    private String operateParam;

    private String pipelineName;

    private String snapshot;

    private Integer containerNum;

    /**
     * 执行流程
     */
    private List<ChaosTaskLogVO> exeStep;

    /**
     * 实验影响的实例信息
     */
    private List<InstanceInfoVO> instanceInfoList;

}
