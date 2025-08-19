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
package org.apache.ozhera.monitor.dao.nutz.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.dao.model.HeraIndicatorDO;
import org.apache.ozhera.monitor.dao.nutz.HeraIndicatorDao;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Slf4j
@Repository
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class HeraIndicatorDaoImpl implements HeraIndicatorDao {

    @Autowired
    private Dao dao;

    @Override
    public HeraIndicatorDO getById(Long id) {
        try {
            SimpleCriteria cri = new SimpleCriteria();
            cri.where().and("id", "=", id)
                    .and("is_deleted", "=", 0);
            return dao.fetch(HeraIndicatorDO.class, cri);
        } catch (Exception e) {
            log.error("根据ID查询指标失败, id={}", id, e);
            return null;
        }

    }

    @Override
    public List<HeraIndicatorDO> query(Cnd cnd) {
        return dao.query(HeraIndicatorDO.class, cnd);
    }

    @Override
    public PageData<List<HeraIndicatorDO>> queryWithPagination(Cnd cnd, int page, int pageSize) {
        try {
            // 执行查询
            List<HeraIndicatorDO> list = dao.query(HeraIndicatorDO.class, cnd, dao.createPager(page, pageSize));
            int total = dao.count(HeraIndicatorDO.class, cnd);

            // 构建分页结果
            PageData<List<HeraIndicatorDO>> pageData = new PageData<>();
            pageData.setList(list);
            pageData.setTotal((long) total);
            pageData.setPage(page);
            pageData.setPageSize(pageSize);

            return pageData;
        } catch (Exception e) {
            log.error("分页查询指标列表失败, cnd={}, page={}, pageSize={}", cnd, page, pageSize, e);
            return new PageData<>();
        }
    }

    @Override
    public HeraIndicatorDO insert(HeraIndicatorDO indicator) {
        try {
            // 设置默认值
            if (indicator.getIndicatorStatus() == null) {
                indicator.setIndicatorStatus(1);
            }
            if (indicator.getIsDeleted() == null) {
                indicator.setIsDeleted(0);
            }
            if (indicator.getCreatedAt() == null) {
                indicator.setCreatedAt(new Date());
            }
            if (indicator.getUpdatedAt() == null) {
                indicator.setUpdatedAt(new Date());
            }
            if (indicator.getDashboardUrl() == null) {
                indicator.setDashboardUrl("");
            }
            if (indicator.getMetricName() == null) {
                indicator.setMetricName("");
            }
            return dao.insert(indicator);
        } catch (Exception e) {
            log.error("Insert indicator error, indicator={}", indicator, e);
            return null;
        }
    }

    @Override
    public int update(HeraIndicatorDO indicator) {
        try {
            indicator.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(indicator);
        } catch (Exception e) {
            log.error("Update indicator error, indicator={}", indicator, e);
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            HeraIndicatorDO indicator = new HeraIndicatorDO();
            indicator.setId(id);
            indicator.setIsDeleted(1);
            indicator.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(indicator);
        } catch (Exception e) {
            log.error("Delete indicator error, id={}", id, e);
            return 0;
        }
    }

    @Override
    public List<HeraIndicatorDO> queryByCreator(String creator) {
        Cnd cnd = Cnd.where("creator", "=", creator)
                .and("is_deleted", "=", 0);
        return dao.query(HeraIndicatorDO.class, cnd);
    }

    @Override
    public HeraIndicatorDO queryByName(String indicatorName) {
        Cnd cnd = Cnd.where("indicator_name", "like", "%" + indicatorName + "%")
                .and("is_deleted", "=", 0);
        return dao.fetch(HeraIndicatorDO.class, cnd);
    }

    @Override
    public HeraIndicatorDO queryByMetricName(String metricName) {
        Cnd cnd = Cnd.where("metric_name", "=", metricName)
                .and("is_deleted", "=", 0);
        return dao.fetch(HeraIndicatorDO.class, cnd);
    }
}