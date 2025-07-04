/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.monitor.bo.bizmetrics;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 业务指标高级查询请求DTO
 */
@Data
public class MetricCtxQueryReq {

    /**
     * 指标ID
     */
    private Long metricId;

    /**
     * 指标类型，可选，如"counter"、"gauge"等
     */
    private String metricType;

    /**
     * 指标数据过滤条件，用于metricData内部字段的复杂查询
     * 支持精确匹配、模糊匹配和范围查询，例如：
     * 1. 精确匹配: {"status": "SUCCESS"}
     * 2. 模糊匹配: {"errorInfo": "*timeout*"}
     * 3. 范围查询: {"duration": {"range": {"gte": 100, "lte": 200}}}
     */
    private Map<String, Object> metricDataFilter;

    /**
     * 查询开始时间戳
     */
    @NotNull(message = "开始时间不能为空")
    private Long startTime;

    /**
     * 查询结束时间戳
     */
    @NotNull(message = "结束时间不能为空")
    private Long endTime;

    /**
     * 页码，默认为1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认为10
     */
    private Integer pageSize = 10;
}