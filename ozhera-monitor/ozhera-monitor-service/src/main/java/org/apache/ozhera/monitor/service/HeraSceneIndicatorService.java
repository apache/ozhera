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
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneIndicatorReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneIndicatorResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;

import java.util.List;

/**
 * 场景-指标关联服务接口
 */
public interface HeraSceneIndicatorService {

    /**
     * 创建或更新场景-指标关联
     * 如果场景和指标间的关联已存在，则更新该关联；
     * 如果关联不存在，则创建新的关联
     *
     * @param req 创建/更新请求，必须包含sceneId和indicatorId
     * @return 操作结果，包含关联ID和操作类型（创建/更新）
     */
    OpFailedInfo createOrUpdateSceneIndicator(HeraSceneIndicatorReq req);

    /**
     * 检查场景与指标之间是否存在关联关系
     *
     * @param req 包含sceneId和indicatorId的请求对象
     * @return 如果指定的场景和指标已关联，则返回true；否则返回false
     */
    boolean isSceneIndicatorAssociated(HeraSceneIndicatorReq req);

    /**
     * 更新场景-指标关联
     *
     * @param req 更新请求
     * @return 是否更新成功
     */
    boolean updateSceneIndicator(HeraSceneIndicatorReq req);

    /**
     * 删除场景-指标关联
     *
     * @param id 关联ID
     * @return 是否删除成功
     */
    boolean deleteSceneIndicator(Long id);

    /**
     * 查询场景-指标关联列表
     *
     * @param req 查询请求
     * @return 关联列表
     */
    PageResult<HeraSceneIndicatorResp> querySceneIndicators(HeraSceneIndicatorReq req);

    /**
     * 获取场景-指标关联详情
     *
     * @param id 关联ID
     * @return 关联详情
     */
    HeraSceneIndicatorResp getSceneIndicatorDetail(Long id);

    /**
     * 根据场景ID查询场景-指标关联列表
     *
     * @param sceneId 场景ID
     * @return 关联列表
     */
    List<HeraSceneIndicatorResp> queryBySceneId(Long sceneId);

    /**
     * 根据指标ID查询场景-指标关联列表
     *
     * @param indicatorId 指标ID
     * @return 关联列表
     */
    List<HeraSceneIndicatorResp> queryByIndicatorId(Long indicatorId);
}