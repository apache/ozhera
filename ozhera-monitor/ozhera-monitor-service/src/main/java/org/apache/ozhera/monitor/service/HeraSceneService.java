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
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;

/**
 * 场景服务接口
 */
public interface HeraSceneService {
    /**
     * 创建场景
     *
     * @param req 创建请求
     * @return 场景ID
     */
    Long createScene(HeraSceneReq req);

    /**
     * 更新场景
     *
     * @param req 更新请求
     * @return 是否更新成功
     */
    boolean updateScene(HeraSceneReq req);

    /**
     * 删除场景
     *
     * @param req 删除请求
     * @return 是否删除成功
     */
    OpFailedInfo deleteScene(HeraSceneReq req);

    /**
     * 查询场景列表
     *
     * @param req 查询请求
     * @return 场景列表
     */
    PageResult<HeraSceneResp> queryScenes(HeraSceneReq req);

    /**
     * 获取场景详情
     *
     * @param req 查询请求
     * @return 场景详情
     */
    HeraSceneResp getSceneDetail(HeraSceneReq req);

    /**
     * 启用场景
     *
     * @param req 启用请求
     * @return 是否启用成功
     */
    boolean enableScene(HeraSceneReq req);

    /**
     * 禁用场景
     *
     * @param req 禁用请求
     * @return 是否禁用成功
     */
    boolean disableScene(HeraSceneReq req);
}