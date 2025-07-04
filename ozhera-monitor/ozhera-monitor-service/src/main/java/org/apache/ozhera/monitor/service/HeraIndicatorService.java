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
package org.apache.ozhera.monitor.service;


import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraIndicatorReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraIndicatorResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;

/**
 * 指标服务接口
 */
public interface HeraIndicatorService {
    /**
     * 创建指标
     *
     * @param req 创建请求
     * @return 指标ID
     */
    Long createIndicator(HeraIndicatorReq req);

    /**
     * 更新指标
     *
     * @param req 更新请求
     * @return 是否成功
     */
    boolean updateIndicator(HeraIndicatorReq req);

    /**
     * 删除指标
     *
     * @param req 删除请求
     * @return 是否操作成功/失败原因
     */
    OpFailedInfo deleteIndicator(HeraIndicatorReq req);

    /**
     * 查询指标列表
     *
     * @param req 查询请求
     * @return 指标列表
     */
    PageResult<HeraIndicatorResp> queryIndicators(HeraIndicatorReq req);

    /**
     * 获取指标详情
     *
     * @param  req 查询请求
     * @return 指标详情
     */
    HeraIndicatorResp getIndicatorDetail(HeraIndicatorReq req);

    /**
     * 内部调用
     *
     * @param id
     * @return
     */
    HeraIndicatorResp getIndicatorDetail(Long id);


    /**
     * 启用指标
     *
     * @param req 启用指标请求
     * @return 是否成功
     */
    boolean enableIndicator(HeraIndicatorReq req);

    /**
     * 禁用指标
     *
     * @param req 禁用指标请求
     * @return 是否成功
     */
    boolean disableIndicator(HeraIndicatorReq req);

    /**
     * 根据指标名称查询指标
     *
     * @param indicatorName 指标名称
     * @return 指标详情
     */
    HeraIndicatorResp getIndicatorByName(String indicatorName);

    /**
     * 根据Prometheus指标名称查询指标
     *
     * @param metricName Prometheus指标名称
     * @return 指标详情
     */
    HeraIndicatorResp getIndicatorByMetricName(String metricName);
}