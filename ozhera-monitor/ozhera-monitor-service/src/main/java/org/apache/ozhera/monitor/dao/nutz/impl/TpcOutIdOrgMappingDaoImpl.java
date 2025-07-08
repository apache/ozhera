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
import org.apache.ozhera.monitor.dao.model.TpcOutIdOrgMappingDO;
import org.apache.ozhera.monitor.dao.nutz.TpcOutIdOrgMappingDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TPC外部ID与组织架构映射DAO实现类
 * 用于管理TPC系统中的外部ID与组织架构路径的映射关系
 * 
 * @author jiangyanze
 */
@Slf4j
@Repository
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class TpcOutIdOrgMappingDaoImpl implements TpcOutIdOrgMappingDao {

    @Autowired
    private Dao dao;

    @Override
    public TpcOutIdOrgMappingDO insert(TpcOutIdOrgMappingDO mapping) {
        if (mapping == null) {
            log.warn("TpcOutIdOrgMappingDO is null, cannot insert");
            return null;
        }

        try {
            // 检查是否存在相同的映射记录
            TpcOutIdOrgMappingDO existing = getByIdPath(mapping.getIdPath());
            if (existing != null && existing.getOutType().equals(mapping.getOutType())) {
                log.warn("Mapping already exists for idPath: {} and outType: {}",
                        mapping.getIdPath(), mapping.getOutType());
                return existing;
            }

            TpcOutIdOrgMappingDO inserted = dao.insert(mapping);
            log.info("Successfully inserted TpcOutIdOrgMapping: id={}, idPath={}, outType={}",
                    inserted.getId(), inserted.getIdPath(), inserted.getOutType());
            return inserted;
        } catch (Exception e) {
            log.error("Failed to insert TpcOutIdOrgMapping: {}", mapping, e);
            return null;
        }
    }

    @Override
    public int delete(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid id for delete: {}", id);
            return 0;
        }

        try {
            int deletedCount = dao.delete(TpcOutIdOrgMappingDO.class, id);
            log.info("Successfully deleted TpcOutIdOrgMapping with id: {}, deletedCount: {}", id, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to delete TpcOutIdOrgMapping with id: {}", id, e);
            return 0;
        }
    }

    @Override
    public int update(TpcOutIdOrgMappingDO mapping) {
        if (mapping == null || mapping.getId() == null) {
            log.warn("TpcOutIdOrgMappingDO or id is null, cannot update");
            return 0;
        }

        try {
            int updatedCount = dao.update(mapping);
            log.info("Successfully updated TpcOutIdOrgMapping: id={}, updatedCount={}", mapping.getId(), updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("Failed to update TpcOutIdOrgMapping: {}", mapping, e);
            return 0;
        }
    }

    @Override
    public TpcOutIdOrgMappingDO getById(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid id for query: {}", id);
            return null;
        }

        try {
            TpcOutIdOrgMappingDO result = dao.fetch(TpcOutIdOrgMappingDO.class, id);
            log.debug("Query TpcOutIdOrgMapping by id: {}, result: {}", id, result != null ? "found" : "not found");
            return result;
        } catch (Exception e) {
            log.error("Failed to query TpcOutIdOrgMapping by id: {}", id, e);
            return null;
        }
    }

    @Override
    public TpcOutIdOrgMappingDO getByIdPath(String idPath) {
        if (idPath == null || idPath.trim().isEmpty()) {
            log.warn("IdPath is null or empty, cannot query");
            return null;
        }

        try {
            TpcOutIdOrgMappingDO result = dao.fetch(TpcOutIdOrgMappingDO.class,
                    Cnd.where("id_path", "=", idPath.trim()));
            log.debug("Query TpcOutIdOrgMapping by idPath: {}, result: {}", idPath,
                    result != null ? result : "not found");
            return result;
        } catch (Exception e) {
            log.error("Failed to query TpcOutIdOrgMapping by idPath: {}", idPath, e);
            return null;
        }
    }

    /**
     * 根据组织架构路径和外部类型查询映射
     * 
     * @param idPath  组织架构路径，如：MI/IT/IT000176/IT000185/IT000189
     * @param outType TPC OutIdTypeEnum的code值
     * @return 映射记录
     */
    public TpcOutIdOrgMappingDO getByIdPathAndOutType(String idPath, Long outType) {
        if (idPath == null || idPath.trim().isEmpty() || outType == null) {
            log.warn("IdPath or outType is invalid: idPath={}, outType={}", idPath, outType);
            return null;
        }

        try {
            TpcOutIdOrgMappingDO result = dao.fetch(TpcOutIdOrgMappingDO.class,
                    Cnd.where("id_path", "=", idPath.trim()).and("out_type", "=", outType));
            log.debug("Query TpcOutIdOrgMapping by idPath: {} and outType: {}, result: {}",
                    idPath, outType, result != null ? "found" : "not found");
            return result;
        } catch (Exception e) {
            log.error("Failed to query TpcOutIdOrgMapping by idPath: {} and outType: {}", idPath, outType, e);
            return null;
        }
    }

    /**
     * 根据外部类型查询所有映射
     * 
     * @param outType TPC OutIdTypeEnum的code值
     * @return 映射记录列表
     */
    public List<TpcOutIdOrgMappingDO> getByOutType(Long outType) {
        if (outType == null) {
            log.warn("OutType is null, cannot query");
            return null;
        }

        try {
            List<TpcOutIdOrgMappingDO> results = dao.query(TpcOutIdOrgMappingDO.class,
                    Cnd.where("out_type", "=", outType));
            log.debug("Query TpcOutIdOrgMapping by outType: {}, count: {}", outType,
                    results != null ? results.size() : 0);
            return results;
        } catch (Exception e) {
            log.error("Failed to query TpcOutIdOrgMapping by outType: {}", outType, e);
            return null;
        }
    }

    /**
     * 根据组织架构路径前缀模糊查询映射（用于查询某个部门及其下级部门）
     * 
     * @param idPathPrefix 组织架构路径前缀，如：MI/IT
     * @return 映射记录列表
     */
    public List<TpcOutIdOrgMappingDO> getByIdPathPrefix(String idPathPrefix) {
        if (idPathPrefix == null || idPathPrefix.trim().isEmpty()) {
            log.warn("IdPathPrefix is null or empty, cannot query");
            return null;
        }

        try {
            String prefix = idPathPrefix.trim();
//            if (!prefix.endsWith("/")) {
//                prefix += "/";
//            }

            List<TpcOutIdOrgMappingDO> results = dao.query(TpcOutIdOrgMappingDO.class,
                    Cnd.where("id_path", "like", prefix + "%"));
            log.debug("Query TpcOutIdOrgMapping by idPathPrefix: {}, count: {}", idPathPrefix,
                    results != null ? results.size() : 0);
            return results;
        } catch (Exception e) {
            log.error("Failed to query TpcOutIdOrgMapping by idPathPrefix: {}", idPathPrefix, e);
            return null;
        }
    }

    /**
     * 批量插入映射记录
     * 
     * @param mappings 映射记录列表
     * @return 成功插入的记录数
     */
    public int batchInsert(List<TpcOutIdOrgMappingDO> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            log.warn("Mappings list is null or empty, cannot batch insert");
            return 0;
        }

        try {
            int successCount = 0;
            for (TpcOutIdOrgMappingDO mapping : mappings) {
                TpcOutIdOrgMappingDO inserted = insert(mapping);
                if (inserted != null) {
                    successCount++;
                }
            }
            log.info("Batch insert completed: total={}, success={}", mappings.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("Failed to batch insert TpcOutIdOrgMappings", e);
            return 0;
        }
    }

    /**
     * 删除指定组织架构路径的所有映射
     * 
     * @param idPath 组织架构路径
     * @return 删除的记录数
     */
    public int deleteByIdPath(String idPath) {
        if (idPath == null || idPath.trim().isEmpty()) {
            log.warn("IdPath is null or empty, cannot delete");
            return 0;
        }

        try {
            int deletedCount = dao.clear(TpcOutIdOrgMappingDO.class,
                    Cnd.where("id_path", "=", idPath.trim()));
            log.info("Successfully deleted TpcOutIdOrgMapping by idPath: {}, deletedCount: {}",
                    idPath, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to delete TpcOutIdOrgMapping by idPath: {}", idPath, e);
            return 0;
        }
    }

    /**
     * 获取所有映射记录（慎用，数据量大时可能影响性能）
     * 
     * @return 所有映射记录
     */
    public List<TpcOutIdOrgMappingDO> getAllMappings() {
        try {
            List<TpcOutIdOrgMappingDO> results = dao.query(TpcOutIdOrgMappingDO.class, null);
            log.debug("Query all TpcOutIdOrgMappings, count: {}", results != null ? results.size() : 0);
            return results;
        } catch (Exception e) {
            log.error("Failed to query all TpcOutIdOrgMappings", e);
            return null;
        }
    }

    /**
     * 统计映射记录总数
     * 
     * @return 记录总数
     */
    public long countAll() {
        try {
            long count = dao.count(TpcOutIdOrgMappingDO.class);
            log.debug("Count all TpcOutIdOrgMappings: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Failed to count TpcOutIdOrgMappings", e);
            return 0;
        }
    }

    /**
     * 检查指定组织架构路径是否存在映射
     * 
     * @param idPath 组织架构路径
     * @return true如果存在映射
     */
    public boolean existsByIdPath(String idPath) {
        return getByIdPath(idPath) != null;
    }
}
