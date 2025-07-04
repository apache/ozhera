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
package org.apache.ozhera.monitor.dao.nutz;

import org.apache.ozhera.monitor.dao.model.HeraSceneIndicatorDO;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;

import java.util.List;

public interface HeraSceneIndicatorDao {

    /**
     * 根据ID查询场景-指标关联
     */
    HeraSceneIndicatorDO getById(Long id);

    /**
     * 根据条件查询场景-指标关联列表
     */
    List<HeraSceneIndicatorDO> query(Cnd cnd);

    /**
     * 创建场景-指标关联
     */
    HeraSceneIndicatorDO insert(HeraSceneIndicatorDO sceneIndicator);

    /**
     * 更新场景-指标关联
     */
    int update(HeraSceneIndicatorDO sceneIndicator);

    /**
     * 删除场景-指标关联（逻辑删除）
     */
    int delete(Long id);

    /**
     * 根据场景ID查询场景-指标关联列表
     */
    List<HeraSceneIndicatorDO> queryBySceneId(Long sceneId);

    /**
     * 根据指标ID查询场景-指标关联列表
     */
    List<HeraSceneIndicatorDO> queryByIndicatorId(Long indicatorId);

    /**
     * 分页查询场景-指标关联列表
     * 
     * @param cnd      查询条件
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    PageData<List<HeraSceneIndicatorDO>> queryWithPagination(Cnd cnd, int page, int pageSize);
}