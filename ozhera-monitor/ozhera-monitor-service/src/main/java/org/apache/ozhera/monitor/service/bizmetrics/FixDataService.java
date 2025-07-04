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
package org.apache.ozhera.monitor.service.bizmetrics;

import com.xiaomi.mone.tpc.common.vo.NodeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.dao.model.HeraIndicatorDO;
import org.apache.ozhera.monitor.dao.model.HeraSceneDO;
import org.apache.ozhera.monitor.dao.nutz.HeraIndicatorDao;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneDao;
import org.apache.ozhera.monitor.service.BusinessMonitorPermissionService;
import org.nutz.dao.Cnd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FixDataService {

    @Autowired
    private BusinessMonitorPermissionService businessMonitorPermissionService;

    @Autowired
    private HeraSceneDao heraSceneDao;

    @Autowired
    private HeraIndicatorDao heraIndicatorDao;

    /**
     * 同步已经存在的场景和指标数据创建到TPC节点
     * 1. 查询所有场景和指标数据(id,名称，创建人)
     * 2. 调用TPC创建teamNode
     * 3. 创建场景节点
     * 4. 创建指标节点
     */
    public void syncTpc() {
        log.info("开始同步场景和指标数据到TPC权限系统");

        try {
            // 1. 查询所有未删除的场景数据
            List<HeraSceneDO> allScenes = queryAllScenes();
            log.info("查询到场景数据总数: {}", allScenes.size());

            // 2. 查询所有未删除的指标数据
            List<HeraIndicatorDO> allIndicators = queryAllIndicators();
            log.info("查询到指标数据总数: {}", allIndicators.size());

            // 3. 收集所有创建人账号，用于创建团队节点
            Set<String> allCreators = new HashSet<>();
            allScenes.forEach(scene -> {
                if (StringUtils.hasText(scene.getCreator())) {
                    allCreators.add(scene.getOwnerId());
                }
            });
            allIndicators.forEach(indicator -> {
                if (StringUtils.hasText(indicator.getCreator())) {
                    allCreators.add(indicator.getCreator());
                }
            });
            log.info("收集到创建人账号总数: {}", allCreators.size());

            // 4. 为每个创建人创建团队节点（如果不存在）
            Map<String, NodeVo> creatorTeamNodes = createTeamNodes(allCreators);

            // 5. 创建场景节点
            syncSceneNodes(allScenes, creatorTeamNodes);

            // 6. 创建指标节点
            syncIndicatorNodes(allIndicators, creatorTeamNodes);

            log.info("TPC数据同步完成!");

        } catch (Exception e) {
            log.error("TPC数据同步失败", e);
            throw new RuntimeException("TPC数据同步失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询所有未删除的场景数据
     */
    private List<HeraSceneDO> queryAllScenes() {
        try {
            Cnd cnd = Cnd.where("is_deleted", "=", 0);
            List<HeraSceneDO> scenes = heraSceneDao.query(cnd);
            log.debug("查询场景数据成功，数量: {}", scenes.size());
            return scenes != null ? scenes : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询所有场景数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询所有未删除的指标数据
     */
    private List<HeraIndicatorDO> queryAllIndicators() {
        try {
            Cnd cnd = Cnd.where("is_deleted", "=", 0);
            List<HeraIndicatorDO> indicators = heraIndicatorDao.query(cnd);
            log.debug("查询指标数据成功，数量: {}", indicators.size());
            return indicators != null ? indicators : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询所有指标数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 为所有创建人创建团队节点
     */
    private Map<String, NodeVo> createTeamNodes(Set<String> creators) {
        Map<String, NodeVo> creatorTeamNodes = new HashMap<>();

        log.info("开始创建团队节点，创建人数量: {}", creators.size());

        for (String creator : creators) {
            if (!StringUtils.hasText(creator)) {
                log.warn("跳过空的创建人账号");
                continue;
            }

            try {
                log.debug("为创建人 {} 创建团队节点", creator);
                NodeVo teamNode = businessMonitorPermissionService.createTeamNode(creator);
                if (teamNode != null) {
                    creatorTeamNodes.put(creator, teamNode);
                    log.debug("成功创建团队节点: 创建人={}, 节点ID={}, 节点名称={}",
                            creator, teamNode.getId(), teamNode.getNodeName());
                } else {
                    log.warn("创建团队节点失败: 创建人={}", creator);
                }
            } catch (Exception e) {
                log.error("为创建人 {} 创建团队节点失败", creator, e);
                // 继续处理其他创建人，不中断整个同步过程
            }
        }

        log.info("团队节点创建完成，成功创建: {}/{}", creatorTeamNodes.size(), creators.size());
        return creatorTeamNodes;
    }

    /**
     * 同步场景节点
     */
    private void syncSceneNodes(List<HeraSceneDO> scenes, Map<String, NodeVo> creatorTeamNodes) {
        log.info("开始同步场景节点，场景数量: {}", scenes.size());

        int successCount = 0;
        int failureCount = 0;

        for (HeraSceneDO scene : scenes) {
            try {
                String creator = scene.getOwnerId();
                if (!StringUtils.hasText(creator)) {
                    log.warn("场景 {} 的创建人为空，跳过", scene.getId());
                    failureCount++;
                    continue;
                }

                NodeVo teamNode = creatorTeamNodes.get(creator);
                if (teamNode == null) {
                    log.warn("场景 {} 的创建人 {} 没有对应的团队节点，跳过", scene.getId(), creator);
                    failureCount++;
                    continue;
                }

                // 获取团队节点的内部组织ID
                Long innerOrgId = businessMonitorPermissionService.getInnerOrgIdForUser(creator);
                if (innerOrgId == null) {
                    log.warn("无法获取创建人 {} 的内部组织ID，跳过场景 {}", creator, scene.getId());
                    failureCount++;
                    continue;
                }

                log.debug("为场景创建节点: ID={}, 名称={}, 创建人={}, 内部组织ID={}",
                        scene.getId(), scene.getSceneName(), creator, innerOrgId);

                NodeVo sceneNode = businessMonitorPermissionService.createSceneNode(
                        innerOrgId,
                        scene.getId(),
                        scene.getSceneName(),
                        creator);

                if (sceneNode != null) {
                    successCount++;
                    log.debug("成功创建场景节点: 场景ID={}, 节点ID={}, 节点名称={}",
                            scene.getId(), sceneNode.getId(), sceneNode.getNodeName());
                } else {
                    failureCount++;
                    log.warn("创建场景节点失败: 场景ID={}", scene.getId());
                }

            } catch (Exception e) {
                failureCount++;
                log.error("同步场景节点失败: 场景ID={}, 错误={}", scene.getId(), e.getMessage(), e);
            }
        }

        log.info("场景节点同步完成，成功: {}, 失败: {}, 总计: {}", successCount, failureCount, scenes.size());
    }

    /**
     * 同步指标节点
     */
    private void syncIndicatorNodes(List<HeraIndicatorDO> indicators, Map<String, NodeVo> creatorTeamNodes) {
        log.info("开始同步指标节点，指标数量: {}", indicators.size());

        int successCount = 0;
        int failureCount = 0;

        for (HeraIndicatorDO indicator : indicators) {
            try {
                String creator = indicator.getCreator();
                if (!StringUtils.hasText(creator)) {
                    log.warn("指标 {} 的创建人为空，跳过", indicator.getId());
                    failureCount++;
                    continue;
                }

                NodeVo teamNode = creatorTeamNodes.get(creator);
                if (teamNode == null) {
                    log.warn("指标 {} 的创建人 {} 没有对应的团队节点，跳过", indicator.getId(), creator);
                    failureCount++;
                    continue;
                }

                // 获取团队节点的内部组织ID
                Long innerOrgId = businessMonitorPermissionService.getInnerOrgIdForUser(creator);
                if (innerOrgId == null) {
                    log.warn("无法获取创建人 {} 的内部组织ID，跳过指标 {}", creator, indicator.getId());
                    failureCount++;
                    continue;
                }

                log.debug("为指标创建节点: ID={}, 名称={}, 创建人={}, 内部组织ID={}",
                        indicator.getId(), indicator.getIndicatorName(), creator, innerOrgId);

                NodeVo indicatorNode = businessMonitorPermissionService.createMetricNode(
                        innerOrgId,
                        indicator.getId(),
                        indicator.getIndicatorName(),
                        creator);

                if (indicatorNode != null) {
                    successCount++;
                    log.debug("成功创建指标节点: 指标ID={}, 节点ID={}, 节点名称={}",
                            indicator.getId(), indicatorNode.getId(), indicatorNode.getNodeName());
                } else {
                    failureCount++;
                    log.warn("创建指标节点失败: 指标ID={}", indicator.getId());
                }

            } catch (Exception e) {
                failureCount++;
                log.error("同步指标节点失败: 指标ID={}, 错误={}", indicator.getId(), e.getMessage(), e);
            }
        }

        log.info("指标节点同步完成，成功: {}, 失败: {}, 总计: {}", successCount, failureCount, indicators.size());
    }
}
