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

import java.util.Date;

/**
 * 指标响应对象
 */
@Data
public class HeraIndicatorResp {
    /**
     * 指标ID
     */
    private Long id;

    /**
     * 指标名称
     */
    private String indicatorName;

    /**
     * 指标描述
     */
    private String indicatorDesc;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 指标状态：0-禁用，1-启用
     */
    private Integer indicatorStatus;

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

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 指标监控看板
     */
    private String dashboardUrl;

    /**
     * 部门id路径
     */
    private String idPath;

    /**
     * 部门名称路径
     */
    private String namePath;

    /**
     * 当前用户对该指标的操作权限
     */
    private ResourcePermission permissions;
}