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

/**
 * 场景-指标关联响应对象
 */
@Data
public class HeraSceneIndicatorResp {
    /**
     * 关联ID
     */
    private Long id;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 指标ID
     */
    private Long indicatorId;

    /**
     * 指标名称
     */
    private String indicatorName;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 指标监控看板
     */
    private String dashboardUrl;

    /**
     * 指标类型：0-counter，1-gauge
     * <pre>
     *  counter:
     *      适用场景：累计计数
     *      值的特性：只能递增
     *      大盘统计方式：rate，计算指定时间范围内指标数据的变化率
     *  Gauge:
     *      适用场景：瞬时值测量
     *      值的特性：可以任意增减
     *      大盘统计方式：点位显示原始值
     * </pre>
     */
    private Integer indicatorType;
}