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
import org.apache.ozhera.monitor.dao.model.HeraSceneDO;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneDao;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Repository
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class HeraSceneDaoImpl implements HeraSceneDao {

    @Autowired
    private Dao dao;

    @Override
    public PageData<List<HeraSceneDO>> queryByCond(HeraSceneDO heraScene, int page, int pageSize,
                                                   String sortBy, String sortRule) {
        try {
            // 创建查询条件
            SimpleCriteria cri = new SimpleCriteria();

            // 设置基本条件 - 未删除的记录
            cri.where().and("is_deleted", "=", 0);

            // 添加查询条件
            if (heraScene != null) {
                // 场景名称模糊查询
                if (heraScene.getSceneName() != null && !heraScene.getSceneName().isEmpty()) {
                    cri.where().and("scene_name", "like", "%" + heraScene.getSceneName() + "%");
                }

                // 场景类型精确匹配
                if (heraScene.getSceneType() != null) {
                    cri.where().and("scene_type", "=", heraScene.getSceneType());
                }

                // 负责人ID精确匹配
                if (heraScene.getOwnerId() != null && !heraScene.getOwnerId().isEmpty()) {
                    cri.where().and("owner_id", "=", heraScene.getOwnerId());
                }

                // 组织ID路径精确匹配
                if (StringUtils.hasText(heraScene.getIdPath())) {
                    cri.where().and("id_path", "=", heraScene.getIdPath());
                }

                // 组织名称路径精确匹配
                if (StringUtils.hasText(heraScene.getNamePath())) {
                    cri.where().and("name_path", "=", heraScene.getNamePath());
                }

                // 场景状态精确匹配
                if (heraScene.getSceneStatus() != null) {
                    cri.where().and("scene_status", "=", heraScene.getSceneStatus());
                }
            }

            // 设置排序
            if (sortBy != null && !sortBy.isEmpty()) {
                cri.orderBy(sortBy, "desc".equalsIgnoreCase(sortRule) ? "desc" : "asc");
            } else {
                cri.orderBy("created_at", "desc");
            }

            // 执行查询
            List<HeraSceneDO> list = dao.query(HeraSceneDO.class, cri, dao.createPager(page, pageSize));
            int total = dao.count(HeraSceneDO.class, cri);

            // 构建分页结果
            PageData<List<HeraSceneDO>> pageData = new PageData<>();
            pageData.setList(list);
            pageData.setTotal((long) total);
            pageData.setPage(page);
            pageData.setPageSize(pageSize);

            return pageData;
        } catch (Exception e) {
            log.error("查询场景列表失败, scene={}, page={}, pageSize={}", heraScene, page, pageSize, e);
            return new PageData<>();
        }
    }

    @Override
    public HeraSceneDO getById(Long id) {
        try {
            SimpleCriteria cri = new SimpleCriteria();
            cri.where().and("id", "=", id)
                    .and("is_deleted", "=", 0);
            return dao.fetch(HeraSceneDO.class, cri);
        } catch (Exception e) {
            log.error("根据ID查询场景失败, id={}", id, e);
            return null;
        }
    }

    @Override
    public boolean insert(HeraSceneDO scene) {
        try {
            // 设置默认值
            if (scene.getSceneStatus() == null) {
                scene.setSceneStatus(1);
            }
            if (scene.getIsDeleted() == null) {
                scene.setIsDeleted(0);
            }
            if (scene.getCreatedAt() == null) {
                scene.setCreatedAt(new Date());
            }
            if (scene.getUpdatedAt() == null) {
                scene.setUpdatedAt(new Date());
            }
            return dao.insert(scene) != null;
        } catch (Exception e) {
            log.error("Insert scene error, scene={}", scene, e);
            return false;
        }
    }

    @Override
    public boolean update(HeraSceneDO scene) {
        try {
            scene.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(scene) > 0;
        } catch (Exception e) {
            log.error("Update scene error, scene={}", scene, e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            HeraSceneDO scene = new HeraSceneDO();
            scene.setId(id);
            scene.setIsDeleted(1);
            scene.setUpdatedAt(new Date());
            return dao.updateIgnoreNull(scene) > 0;
        } catch (Exception e) {
            log.error("Delete scene error, id={}", id, e);
            return false;
        }
    }

    @Override
    public PageData<List<HeraSceneDO>> queryByCondWithPermission(HeraSceneDO heraScene, List<Long> sceneIds,
            int page, int pageSize, String sortBy, String sortRule) {
        try {
            // 如果权限列表为空，直接返回空结果
            if (sceneIds == null || sceneIds.isEmpty()) {
                PageData<List<HeraSceneDO>> emptyPageData = new PageData<>();
                emptyPageData.setList(List.of());
                emptyPageData.setTotal(0L);
                emptyPageData.setPage(page);
                emptyPageData.setPageSize(pageSize);
                return emptyPageData;
            }

            // 创建查询条件
            SimpleCriteria cri = new SimpleCriteria();

            // 设置基本条件 - 未删除的记录
            cri.where().and("is_deleted", "=", 0);

            // 添加权限过滤条件 - 只查询用户有权限的场景
            cri.where().and("id", "in", sceneIds);

            // 添加其他查询条件
            if (heraScene != null) {
                // 场景名称模糊查询
                if (heraScene.getSceneName() != null && !heraScene.getSceneName().isEmpty()) {
                    cri.where().and("scene_name", "like", "%" + heraScene.getSceneName() + "%");
                }

                // 场景类型精确匹配
                if (heraScene.getSceneType() != null) {
                    cri.where().and("scene_type", "=", heraScene.getSceneType());
                }

                // 负责人ID精确匹配
                if (heraScene.getOwnerId() != null && !heraScene.getOwnerId().isEmpty()) {
                    cri.where().and("owner_id", "=", heraScene.getOwnerId());
                }

                // 组织ID路径精确匹配
                if (StringUtils.hasText(heraScene.getIdPath())) {
                    cri.where().and("id_path", "=", heraScene.getIdPath());
                }

                // 组织名称路径精确匹配
                if (StringUtils.hasText(heraScene.getNamePath())) {
                    cri.where().and("name_path", "=", heraScene.getNamePath());
                }

                // 场景状态精确匹配
                if (heraScene.getSceneStatus() != null) {
                    cri.where().and("scene_status", "=", heraScene.getSceneStatus());
                }
            }

            // 设置排序
            if (sortBy != null && !sortBy.isEmpty()) {
                cri.orderBy(sortBy, "desc".equalsIgnoreCase(sortRule) ? "desc" : "asc");
            } else {
                cri.orderBy("created_at", "desc");
            }

            // 执行查询
            List<HeraSceneDO> list = dao.query(HeraSceneDO.class, cri, dao.createPager(page, pageSize));
            int total = dao.count(HeraSceneDO.class, cri);

            // 构建分页结果
            PageData<List<HeraSceneDO>> pageData = new PageData<>();
            pageData.setList(list);
            pageData.setTotal((long) total);
            pageData.setPage(page);
            pageData.setPageSize(pageSize);

            log.debug("Query scenes with permission - sceneIds: {}, total: {}, returned: {}",
                    sceneIds.size(), total, list.size());

            return pageData;
        } catch (Exception e) {
            log.error("查询有权限的场景列表失败, scene={}, sceneIds={}, page={}, pageSize={}",
                    heraScene, sceneIds.size(), page, pageSize, e);
            return new PageData<>();
        }
    }

    @Override
    public List<HeraSceneDO> query(Cnd cnd) {
        try {
            return dao.query(HeraSceneDO.class, cnd);
        } catch (Exception e) {
            log.error("根据条件查询场景列表失败, cnd={}", cnd, e);
            return new ArrayList<>();
        }
    }
}