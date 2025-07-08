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
 * 指标请求对象
 */
@Data
public class HeraIndicatorReq {
    /**
     * 指标ID
     */
    private Long id;

    /**
     * 场景ID
     */
    private Long sceneId;

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
     * 部门id路径
     */
    private String idPath;

    /**
     * 部门名称路径
     */
    private String namePath;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 当前操作人账号
     */
    private String operatorAccount;
}