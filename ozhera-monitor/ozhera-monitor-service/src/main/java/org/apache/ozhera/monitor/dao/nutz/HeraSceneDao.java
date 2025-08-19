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

import org.apache.ozhera.monitor.dao.model.HeraSceneDO;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;

import java.util.List;

/**
 * 场景DAO接口
 */
public interface HeraSceneDao {

    PageData<List<HeraSceneDO>> queryByCond(HeraSceneDO heraScene, int page, int pageSize, String sortBy,
                                            String sortRule);

    /**
     * 根据条件和权限查询场景列表
     *
     * @param heraScene 查询条件
     * @param sceneIds  用户有权限的场景ID列表
     * @param page      页码
     * @param pageSize  每页大小
     * @param sortBy    排序字段
     * @param sortRule  排序规则
     * @return 分页查询结果
     */
    PageData<List<HeraSceneDO>> queryByCondWithPermission(HeraSceneDO heraScene, List<Long> sceneIds,
            int page, int pageSize, String sortBy, String sortRule);

    /**
     * 根据ID获取场景
     *
     * @param id 场景ID
     * @return 场景对象
     */
    HeraSceneDO getById(Long id);

    /**
     * 根据条件查询场景列表
     *
     * @param cnd 查询条件
     * @return 场景列表
     */
    List<HeraSceneDO> query(Cnd cnd);

    /**
     * 插入场景
     *
     * @param scene 场景对象
     */
    boolean insert(HeraSceneDO scene);

    /**
     * 更新场景
     *
     * @param scene 场景对象
     * @return 影响行数
     */
    boolean update(HeraSceneDO scene);

    /**
     * 删除场景
     *
     * @param id 场景ID
     * @return 影响行数
     */
    boolean delete(Long id);
}