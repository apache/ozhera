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
import org.apache.ozhera.monitor.dao.model.HeraSceneIndicatorDO;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneIndicatorDao;
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
public class HeraSceneIndicatorDaoImpl implements HeraSceneIndicatorDao {

    @Autowired
    private Dao dao;

    @Override
    public HeraSceneIndicatorDO getById(Long id) {
        try {
            SimpleCriteria cri = new SimpleCriteria();
            cri.where().and("id", "=", id)
                    .and("is_deleted", "=", 0);
            return dao.fetch(HeraSceneIndicatorDO.class, cri);
        } catch (Exception e) {
            log.error("根据ID查询场景指标失败, id={}", id, e);
            return null;
        }
    }

    @Override
    public List<HeraSceneIndicatorDO> query(Cnd cnd) {
        return dao.query(HeraSceneIndicatorDO.class, cnd);
    }

    @Override
    public HeraSceneIndicatorDO insert(HeraSceneIndicatorDO sceneIndicator) {
        try {
            // 设置默认值
            if (sceneIndicator.getIsDeleted() == null) {
                sceneIndicator.setIsDeleted(0);
            }
            if (sceneIndicator.getCreatedAt() == null) {
                sceneIndicator.setCreatedAt(new Date());
            }
            if (sceneIndicator.getUpdatedAt() == null) {
                sceneIndicator.setUpdatedAt(new Date());
            }
            return dao.insert(sceneIndicator);
        } catch (Exception e) {
            log.error("Insert scene indicator error, sceneIndicator={}", sceneIndicator, e);
            return null;
        }
    }

    @Override
    public int update(HeraSceneIndicatorDO sceneIndicator) {
        try {
            sceneIndicator.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(sceneIndicator);
        } catch (Exception e) {
            log.error("Update scene indicator error, sceneIndicator={}", sceneIndicator, e);
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            HeraSceneIndicatorDO sceneIndicator = new HeraSceneIndicatorDO();
            sceneIndicator.setId(id);
            sceneIndicator.setIsDeleted(1);
            sceneIndicator.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(sceneIndicator);
        } catch (Exception e) {
            log.error("Delete scene indicator error, id={}", id, e);
            return 0;
        }
    }

    @Override
    public List<HeraSceneIndicatorDO> queryBySceneId(Long sceneId) {
        Cnd cnd = Cnd.where("scene_id", "=", sceneId)
                .and("is_deleted", "=", 0);
        return dao.query(HeraSceneIndicatorDO.class, cnd);
    }

    @Override
    public List<HeraSceneIndicatorDO> queryByIndicatorId(Long indicatorId) {
        Cnd cnd = Cnd.where("indicator_id", "=", indicatorId)
                .and("is_deleted", "=", 0);
        return dao.query(HeraSceneIndicatorDO.class, cnd);
    }

    @Override
    public PageData<List<HeraSceneIndicatorDO>>
    queryWithPagination(Cnd cnd, int page, int pageSize) {
        try {
            // 执行查询
            List<HeraSceneIndicatorDO> list = dao.query(HeraSceneIndicatorDO.class, cnd,
                    dao.createPager(page, pageSize));
            int total = dao.count(HeraSceneIndicatorDO.class, cnd);

            // 构建分页结果
            PageData<List<HeraSceneIndicatorDO>> pageData = new PageData<>();
            pageData.setList(list);
            pageData.setTotal((long) total);
            pageData.setPage(page);
            pageData.setPageSize(pageSize);

            return pageData;
        } catch (Exception e) {
            log.error("分页查询场景-指标关联列表失败, cnd={}, page={}, pageSize={}", cnd, page, pageSize, e);
            return new PageData<>();
        }
    }
}