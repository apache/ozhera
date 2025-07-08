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

import org.apache.ozhera.monitor.dao.model.HeraIndicatorDO;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;

import java.util.List;

public interface HeraIndicatorDao {

    /**
     * 根据ID查询指标
     */
    HeraIndicatorDO getById(Long id);

    /**
     * 根据条件查询指标列表
     */
    List<HeraIndicatorDO> query(Cnd cnd);

    /**
     * 分页查询指标列表
     * 
     * @param cnd      查询条件
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    PageData<List<HeraIndicatorDO>> queryWithPagination(Cnd cnd, int page, int pageSize);

    /**
     * 创建指标
     */
    HeraIndicatorDO insert(HeraIndicatorDO indicator);

    /**
     * 更新指标
     */
    int update(HeraIndicatorDO indicator);

    /**
     * 删除指标（逻辑删除）
     */
    int delete(Long id);

    /**
     * 根据创建人查询指标列表
     */
    List<HeraIndicatorDO> queryByCreator(String creator);

    /**
     * 根据指标名称查询指标
     */
    HeraIndicatorDO queryByName(String indicatorName);

    /**
     * 根据Prometheus指标名称查询指标
     */
    HeraIndicatorDO queryByMetricName(String metricName);
}