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
import org.apache.ozhera.monitor.annotation.RequireScenePermission;
import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;
import org.apache.ozhera.monitor.bo.bizmetrics.ResourcePermission;
import org.apache.ozhera.monitor.dao.model.HeraSceneDO;
import org.apache.ozhera.monitor.dao.model.HeraSceneIndicatorDO;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneDao;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneIndicatorDao;
import org.apache.ozhera.monitor.enums.BizMetricsSecneStatusEnum;
import org.apache.ozhera.monitor.result.ErrorInfoConstants;
import org.apache.ozhera.monitor.service.BusinessMonitorPermissionService;
import org.apache.ozhera.monitor.service.HeraSceneService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 场景服务实现类
 */
@Slf4j
@Service
public class HeraSceneServiceImpl implements HeraSceneService {

    // 业务常量定义
    private static final int DEFAULT_SCENE_TYPE = 1; // 默认场景类型：业务场景
    private static final int DEFAULT_NOTIFY_TYPE = 1; // 默认通知类型：飞书
    private static final int NOT_DELETED = 0; // 未删除状态
    private static final int ENABLED_FLAG = 1; // 启用标志
    private static final int DISABLED_FLAG = 0; // 禁用标志

    @Autowired
    private HeraSceneDao heraSceneDao;

    @Autowired
    private HeraSceneIndicatorDao heraSceneIndicatorDao;

    @Autowired
    private BusinessMonitorPermissionService businessMonitorPermissionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BusinessChangeNotificationService businessChangeNotificationService;
    @Autowired
    private BizCustomMetricsConfig bizCustomMetricsConfig;

    @Override
    public Long createScene(HeraSceneReq req) {
        try {
            // 1. 构建场景对象
            HeraSceneDO scene = buildSceneFromRequest(req);

            // 2. 插入数据库
            boolean insertSuccess = heraSceneDao.insert(scene);
            if (!insertSuccess) {
                log.error("场景数据插入失败, req={}", req);
                return null;
            }

            // 3. 创建TPC权限节点
            boolean tpcNodeCreated = createTpcPermissionNodes(req, scene.getId());
            if (!tpcNodeCreated) {
                log.warn("TPC权限节点创建失败，但场景已创建成功, sceneId={}", scene.getId());
            }

            // 4. 发送变更通知
            try {
                businessChangeNotificationService.notifySceneChange(
                        scene.getId(),
                        scene.getSceneName(),
                        BusinessChangeNotificationService.SceneOperationType.CREATE,
                        req.getOperatorAccount(),
                        "场景创建成功");
            } catch (Exception e) {
                log.warn("发送场景创建通知失败，但场景创建成功, sceneId={}", scene.getId(), e);
            }

            return scene.getId();
        } catch (Exception e) {
            log.error("创建场景失败, req={}", req, e);
            return null;
        }
    }

    @Override
    @RequireScenePermission(operation = "更新场景")
    public boolean updateScene(HeraSceneReq req) {
        try {
            // 1. 获取原场景信息用于通知
            HeraSceneDO originalScene = heraSceneDao.getById(req.getSceneId());

            // 2. 构建更新对象
            HeraSceneDO scene = buildUpdateSceneFromRequest(req);

            // 3. 执行更新
            boolean updateSuccess = heraSceneDao.update(scene);

            // 4. 发送变更通知
            if (updateSuccess) {
                try {
                    String sceneName = originalScene != null ? originalScene.getSceneName() : "未知场景";
                    businessChangeNotificationService.notifySceneChange(
                            req.getSceneId(),
                            sceneName,
                            BusinessChangeNotificationService.SceneOperationType.UPDATE,
                            req.getOperatorAccount(),
                            "场景信息更新");
                } catch (Exception e) {
                    log.warn("发送场景更新通知失败，但场景更新成功, sceneId={}", req.getSceneId(), e);
                }
            }

            return updateSuccess;
        } catch (Exception e) {
            log.error("更新场景失败, req={}", req, e);
            return false;
        }
    }

    @Override
    @RequireScenePermission(operation = "删除场景")
    public OpFailedInfo deleteScene(HeraSceneReq req) {
        try {
            // 1. 获取原场景信息用于通知
            HeraSceneDO originalScene = heraSceneDao.getById(req.getSceneId());

            // 2. 检查场景是否存在关联的指标
            List<HeraSceneIndicatorDO> sceneIndicatorMappingList = heraSceneIndicatorDao
                    .queryBySceneId(req.getSceneId());

            if (hasAssociatedIndicators(sceneIndicatorMappingList)) {
                return buildFailedResult(ErrorInfoConstants.SCENE_OP_DELETE_ERROR_BY_MAPPING);
            }

            // 3. 执行删除操作
            boolean deleteSuccess = heraSceneDao.delete(req.getSceneId());
            if (deleteSuccess) {
                // 4. 发送变更通知
                try {
                    String sceneName = originalScene != null ? originalScene.getSceneName() : "未知场景";
                    businessChangeNotificationService.notifySceneChange(
                            req.getSceneId(),
                            sceneName,
                            BusinessChangeNotificationService.SceneOperationType.DELETE,
                            req.getOperatorAccount(),
                            "场景删除成功");
                } catch (Exception e) {
                    log.warn("发送场景删除通知失败，但场景删除成功, sceneId={}", req.getSceneId(), e);
                }

                return buildSuccessResult();
            } else {
                return buildFailedResult(ErrorInfoConstants.SCENE_HAS_BEEN_DELETED);
            }
        } catch (Exception e) {
            log.error("删除场景失败, id={}", req.getSceneId(), e);
            return buildFailedResult(ErrorInfoConstants.SERVER_INTERNAL_ERROR);
        }
    }

    @Override
    public PageResult<HeraSceneResp> queryScenes(HeraSceneReq req) {
        try {
            // 构建查询条件
            HeraSceneDO queryDO = buildQueryCondition(req);

            // 获取用户权限的场景ID列表
            List<Long> authorizedSceneIds = getAuthorizedSceneIds(req.getOperatorAccount());
            if (authorizedSceneIds.isEmpty()) {
                log.debug("User {} has no scene permissions, returning empty result", req.getOperatorAccount());
                return buildEmptyPageResult(req);
            }

            // 执行分页查询
            PageData<List<HeraSceneDO>> pageData = heraSceneDao.queryByCondWithPermission(
                    queryDO, authorizedSceneIds, req.getPageNo(), req.getPageSize(), "created_at", "desc");

            // 转换并返回结果
            return buildPageResult(pageData, req);
        } catch (Exception e) {
            log.error("查询场景列表失败, req={}", req, e);
            return new PageResult<>();
        }
    }

    @Override
    @RequireScenePermission(operation = "获取场景详情")
    public HeraSceneResp getSceneDetail(HeraSceneReq req) {
        try {
            HeraSceneDO scene = heraSceneDao.getById(req.getSceneId());
            if (scene == null) {
                return null;
            }

            HeraSceneResp resp = convertToResp(scene);

            // 计算并设置权限信息
            String ownerId = scene.getOwnerId() != null ? scene.getOwnerId() : scene.getCreator();
            ResourcePermission permission = businessMonitorPermissionService
                    .calculateScenePermission(
                            scene.getId(), ownerId, req.getOperatorAccount());
            resp.setPermissions(permission);

            log.debug("Retrieved scene detail with permissions for user {}, sceneId={}",
                    req.getOperatorAccount(), req.getSceneId());

            return resp;
        } catch (Exception e) {
            log.error("获取场景详情失败, id={}", req.getSceneId(), e);
            return null;
        }
    }

    @Override
    @RequireScenePermission(operation = "启用场景")
    public boolean enableScene(HeraSceneReq req) {
        try {
            // 1. 获取原场景信息用于通知
            HeraSceneDO originalScene = heraSceneDao.getById(req.getSceneId());

            // 2. 执行启用操作
            boolean success = updateSceneStatus(req.getSceneId(), BizMetricsSecneStatusEnum.ENABLED, "启用");

            // 3. 发送变更通知
            if (success) {
                try {
                    String sceneName = originalScene != null ? originalScene.getSceneName() : "未知场景";
                    businessChangeNotificationService.notifySceneChange(
                            req.getSceneId(),
                            sceneName,
                            BusinessChangeNotificationService.SceneOperationType.ENABLE,
                            req.getOperatorAccount(),
                            "场景已启用");
                } catch (Exception e) {
                    log.warn("发送场景启用通知失败，但场景启用成功, sceneId={}", req.getSceneId(), e);
                }
            }

            return success;
        } catch (Exception e) {
            log.error("启用场景失败, sceneId={}", req.getSceneId(), e);
            return false;
        }
    }

    @Override
    @RequireScenePermission(operation = "禁用场景")
    public boolean disableScene(HeraSceneReq req) {
        try {
            // 1. 获取原场景信息用于通知
            HeraSceneDO originalScene = heraSceneDao.getById(req.getSceneId());

            // 2. 执行禁用操作
            boolean success = updateSceneStatus(req.getSceneId(), BizMetricsSecneStatusEnum.DISABLED, "禁用");

            // 3. 发送变更通知
            if (success) {
                try {
                    String sceneName = originalScene != null ? originalScene.getSceneName() : "未知场景";
                    businessChangeNotificationService.notifySceneChange(
                            req.getSceneId(),
                            sceneName,
                            BusinessChangeNotificationService.SceneOperationType.DISABLE,
                            req.getOperatorAccount(),
                            "场景已禁用");
                } catch (Exception e) {
                    log.warn("发送场景禁用通知失败，但场景禁用成功, sceneId={}", req.getSceneId(), e);
                }
            }

            return success;
        } catch (Exception e) {
            log.error("禁用场景失败, sceneId={}", req.getSceneId(), e);
            return false;
        }
    }

    /**
     * 根据请求构建场景DO对象并设置默认值
     */
    private HeraSceneDO buildSceneFromRequest(HeraSceneReq req) {
        HeraSceneDO scene = new HeraSceneDO();
        BeanUtils.copyProperties(req, scene);

        // 设置场景状态
        setSceneStatus(scene, req.getEnabled());

        // 设置默认值
        setDefaultSceneValues(scene, req.getOwnerId());

        return scene;
    }

    /**
     * 构建更新场景对象，合并原始数据和请求数据
     */
    private HeraSceneDO buildUpdateSceneFromRequest(HeraSceneReq req) {
        HeraSceneDO sceneToUpdate = new HeraSceneDO();
        BeanUtils.copyProperties(req, sceneToUpdate);
        sceneToUpdate.setId(req.getSceneId());

        // 合并原始值（如果请求中未指定）
        mergeOriginalValues(sceneToUpdate, req);

        return sceneToUpdate;
    }

    /**
     * 合并原始值到更新对象中
     */
    private void mergeOriginalValues(HeraSceneDO sceneToUpdate, HeraSceneReq req) {
        if (sceneToUpdate.getSceneStatus() == null) {
            sceneToUpdate.setSceneStatus(
                    req.getEnabled() != null && req.getEnabled() ? BizMetricsSecneStatusEnum.ENABLED.getCode()
                            : BizMetricsSecneStatusEnum.DISABLED.getCode());
        }
        if (sceneToUpdate.getSceneType() == null) {
            sceneToUpdate.setSceneType(req.getSceneType() != null ? req.getSceneType() : DEFAULT_SCENE_TYPE);
        }
        if (sceneToUpdate.getOwnerId() == null) {
            sceneToUpdate.setOwnerId(req.getOwnerId() != null ? req.getOwnerId() : req.getOperatorAccount());
            sceneToUpdate.setOwnerName(req.getOwnerId() != null ? req.getOwnerId() : req.getOperatorAccount());
        }
        if (sceneToUpdate.getNotifyType() == null) {
            sceneToUpdate.setNotifyType(req.getNotifyType() != null ? req.getNotifyType() : DEFAULT_NOTIFY_TYPE);
        }
        if (sceneToUpdate.getNotifyTarget() == null) {
            sceneToUpdate.setNotifyTarget(req.getOwnerId() != null ? req.getOwnerId() : req.getOperatorAccount());
        }

        // 保持不变的字段
        if (sceneToUpdate.getCreator() == null) {
            sceneToUpdate.setCreator(req.getOperatorAccount());
        }
        if (sceneToUpdate.getIsDeleted() == null) {
            sceneToUpdate.setIsDeleted(NOT_DELETED);
        }
    }

    /**
     * 设置场景状态
     */
    private void setSceneStatus(HeraSceneDO scene, Boolean enabled) {
        if (enabled == null || !enabled) {
            scene.setSceneStatus(BizMetricsSecneStatusEnum.DISABLED.getCode());
        } else {
            scene.setSceneStatus(BizMetricsSecneStatusEnum.ENABLED.getCode());
        }
    }

    /**
     * 设置场景的默认值
     */
    private void setDefaultSceneValues(HeraSceneDO scene, String ownerId) {
        // 设置创建者（如果为空则使用拥有者ID）
        if (scene.getCreator() == null) {
            scene.setCreator(ownerId);
        }

        // 设置默认场景类型为业务场景
        if (scene.getSceneType() == null) {
            scene.setSceneType(DEFAULT_SCENE_TYPE);
        }

        // 设置默认通知类型为飞书
        if (scene.getNotifyType() == null) {
            scene.setNotifyType(DEFAULT_NOTIFY_TYPE);
        }

        // 设置拥有者名称（如果为空则使用拥有者ID）
        if (scene.getOwnerName() == null) {
            scene.setOwnerName(ownerId);
        }

        // 设置通知目标（如果为空则使用拥有者ID）
        if (scene.getNotifyTarget() == null) {
            scene.setNotifyTarget(ownerId);
        }

        // 设置为未删除状态
        scene.setIsDeleted(NOT_DELETED);
    }

    /**
     * 创建TPC权限节点
     */
    private boolean createTpcPermissionNodes(HeraSceneReq req, Long sceneId) {
        try {
            // 创建团队节点
            NodeVo teamNode = businessMonitorPermissionService.createTeamNode(req.getOwnerId());
            if (teamNode == null) {
                log.error("创建TPC团队节点失败, ownerId={}", req.getOwnerId());
                return false;
            }

            // 创建场景节点
            NodeVo sceneNode = businessMonitorPermissionService.createSceneNode(
                    teamNode.getOutId(), sceneId, req.getSceneName(), req.getOwnerId());
            if (sceneNode == null) {
                log.error("创建TPC场景节点失败, teamOutId={}, sceneId={}, sceneName={}",
                        teamNode.getOutId(), sceneId, req.getSceneName());
                return false;
            }

            log.info("TPC权限节点创建成功, sceneId={}, teamNodeId={}, sceneNodeId={}",
                    sceneId, teamNode.getOutId(), sceneNode.getOutId());
            return true;
        } catch (Exception e) {
            log.error("创建TPC权限节点异常, sceneId={}", sceneId, e);
            return false;
        }
    }

    /**
     * 统一的场景状态更新方法
     */
    private boolean updateSceneStatus(Long sceneId, BizMetricsSecneStatusEnum status, String operation) {
        try {
            HeraSceneDO scene = new HeraSceneDO();
            scene.setId(sceneId);
            scene.setSceneStatus(status.getCode());
            return heraSceneDao.update(scene);
        } catch (Exception e) {
            log.error("{}场景失败, id={}", operation, sceneId, e);
            return false;
        }
    }

    /**
     * 检查是否存在关联指标
     */
    private boolean hasAssociatedIndicators(List<HeraSceneIndicatorDO> indicatorList) {
        return indicatorList != null && !indicatorList.isEmpty();
    }

    /**
     * 构建查询条件
     */
    private HeraSceneDO buildQueryCondition(HeraSceneReq req) {
        HeraSceneDO queryDO = new HeraSceneDO();
        BeanUtils.copyProperties(req, queryDO);

        // 设置启用状态查询条件
        if (req.getEnabled() != null) {
            queryDO.setSceneStatus(req.getEnabled() ? BizMetricsSecneStatusEnum.ENABLED.getCode()
                    : BizMetricsSecneStatusEnum.DISABLED.getCode());
        }

        return queryDO;
    }

    /**
     * 获取用户有权限的场景ID列表
     */
    private List<Long> getAuthorizedSceneIds(String operatorAccount) {
        List<Long> sceneIds = businessMonitorPermissionService.getSceneNodeIds(operatorAccount);
        return sceneIds != null ? sceneIds : new ArrayList<>();
    }

    /**
     * 构建空的分页结果
     */
    private PageResult<HeraSceneResp> buildEmptyPageResult(HeraSceneReq req) {
        return new PageResult<>(new ArrayList<>(), 0L, req.getPageNo(), req.getPageSize());
    }

    /**
     * 构建分页结果
     */
    private PageResult<HeraSceneResp> buildPageResult(PageData<List<HeraSceneDO>> pageData, HeraSceneReq req) {
        List<HeraSceneDO> sceneList = pageData.getList();
        if (sceneList == null || sceneList.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), pageData.getTotal(), req.getPageNo(), req.getPageSize());
        }

        // 批量计算权限 - 性能优化
        Map<Long, String> sceneInfos = sceneList.stream()
                .collect(Collectors.toMap(
                        HeraSceneDO::getId,
                        scene -> scene.getOwnerId() != null ? scene.getOwnerId() : scene.getCreator()));

        Map<Long, ResourcePermission> permissionMap = businessMonitorPermissionService
                .batchCalculateScenePermissions(sceneInfos, req.getOperatorAccount());

        // 转换为响应对象并设置权限
        List<HeraSceneResp> respList = sceneList.stream()
                .map(scene -> {
                    HeraSceneResp resp = convertToResp(scene);
                    // 设置权限信息
                    ResourcePermission permission = permissionMap
                            .get(scene.getId());
                    resp.setPermissions(permission != null ? permission
                            : ResourcePermission.viewOnlyPermission());
                    return resp;
                })
                .collect(Collectors.toList());

        log.debug("Built page result with {} scenes and calculated permissions for user {}",
                respList.size(), req.getOperatorAccount());

        return new PageResult<>(respList, pageData.getTotal(), req.getPageNo(), req.getPageSize());
    }

    /**
     * 构建成功结果
     */
    private OpFailedInfo buildSuccessResult() {
        return OpFailedInfo.builder().success(true).build();
    }

    /**
     * 构建失败结果
     */
    private OpFailedInfo buildFailedResult(String reason) {
        return OpFailedInfo.builder().success(false).reason(reason).build();
    }

    /**
     * 将DO对象转换为响应对象
     */
    private HeraSceneResp convertToResp(HeraSceneDO scene) {
        HeraSceneResp resp = new HeraSceneResp();
        BeanUtils.copyProperties(scene, resp);
        resp.setSceneId(scene.getId());
        resp.setEnabled(
                scene.getSceneStatus() == BizMetricsSecneStatusEnum.ENABLED.getCode() ? ENABLED_FLAG : DISABLED_FLAG);
        resp.setCreateTime(scene.getCreatedAt().getTime());
        resp.setUpdateTime(scene.getUpdatedAt().getTime());

        /**
         * TODO:快速兼容业务指标聚合看板，现阶段通过配置获取，后续这里需要通过db获取场景层面的监控大盘url
         */
        if(bizCustomMetricsConfig.hasSceneConfig(scene.getId())) {
            resp.setDashboardUrl(bizCustomMetricsConfig.getSceneDashboardUrl(scene.getId()));
        }
        return resp;
    }

}