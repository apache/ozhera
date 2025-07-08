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
import org.apache.ozhera.monitor.annotation.RequireIndicatorPermission;
import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraIndicatorReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraIndicatorResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;
import org.apache.ozhera.monitor.bo.bizmetrics.ResourcePermission;
import org.apache.ozhera.monitor.dao.model.HeraIndicatorDO;
import org.apache.ozhera.monitor.dao.model.HeraSceneIndicatorDO;
import org.apache.ozhera.monitor.dao.nutz.HeraIndicatorDao;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneIndicatorDao;
import org.apache.ozhera.monitor.result.ErrorInfoConstants;
import org.apache.ozhera.monitor.service.BusinessMonitorPermissionService;
import org.apache.ozhera.monitor.service.HeraIndicatorService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标服务实现类
 */
@Slf4j
@Service
public class HeraIndicatorServiceImpl implements HeraIndicatorService {

    // 业务常量定义
    private static final int INDICATOR_ENABLED = 1; // 指标启用状态
    private static final int INDICATOR_DISABLED = 0; // 指标禁用状态
    private static final int NOT_DELETED = 0; // 未删除状态
    private static final String ORDER_BY_CREATED_DESC = "desc"; // 创建时间倒序

    @Autowired
    private HeraIndicatorDao heraIndicatorDao;

    @Autowired
    private HeraSceneIndicatorDao heraSceneIndicatorDao;

    @Autowired
    private BusinessMonitorPermissionService businessMonitorPermissionService;

    @Autowired
    private BusinessChangeNotificationService businessChangeNotificationService;

    @Autowired
    private BizCustomMetricsConfig bizCustomMetricsConfig;

    @Override
    public Long createIndicator(HeraIndicatorReq req) {
        try {
            // 1. 构建并插入指标数据
            HeraIndicatorDO indicator = buildIndicatorFromRequest(req);
            HeraIndicatorDO insertResult = heraIndicatorDao.insert(indicator);

            if (insertResult == null || insertResult.getId() == null) {
                log.error("指标数据插入失败, req={}", req);
                return null;
            }

            // 2. 创建TPC权限节点
            boolean tpcNodeCreated = createTpcPermissionNodes(req, insertResult.getId());
            if (!tpcNodeCreated) {
                log.warn("TPC权限节点创建失败，但指标已创建成功, indicatorId={}", insertResult.getId());
                // 注意：这里不回滚指标创建，因为指标创建成功，只是权限节点创建失败
                // 可根据业务需求决定是否需要回滚
            }

            // 3. 发送变更通知
            try {
                businessChangeNotificationService.notifyIndicatorChange(
                        insertResult.getId(),
                        insertResult.getIndicatorName(),
                        BusinessChangeNotificationService.IndicatorOperationType.CREATE,
                        req.getOperatorAccount(),
                        "指标创建成功");
            } catch (Exception e) {
                log.warn("发送指标创建通知失败，但指标创建成功, indicatorId={}", insertResult.getId(), e);
            }

            return insertResult.getId();
        } catch (Exception e) {
            log.error("创建指标失败, req={}", req, e);
            return null;
        }
    }

    @Override
    @RequireIndicatorPermission(operation = "更新指标")
    public boolean updateIndicator(HeraIndicatorReq req) {
        try {
            // 1. 获取原指标信息用于通知
            HeraIndicatorDO originalIndicator = heraIndicatorDao.getById(req.getId());

            // 2. 执行更新
            HeraIndicatorDO indicator = buildUpdateIndicatorFromRequest(req);
            boolean updateSuccess = heraIndicatorDao.update(indicator) > 0;

            // 3. 发送变更通知
            if (updateSuccess) {
                try {
                    String indicatorName = originalIndicator != null ? originalIndicator.getIndicatorName() : "未知指标";
                    businessChangeNotificationService.notifyIndicatorChange(
                            req.getId(),
                            indicatorName,
                            BusinessChangeNotificationService.IndicatorOperationType.UPDATE,
                            req.getOperatorAccount(),
                            "指标信息更新");
                } catch (Exception e) {
                    log.warn("发送指标更新通知失败，但指标更新成功, indicatorId={}", req.getId(), e);
                }
            }

            return updateSuccess;
        } catch (Exception e) {
            log.error("更新指标失败, req={}", req, e);
            return false;
        }
    }

    @Override
    @RequireIndicatorPermission(operation = "删除指标")
    public OpFailedInfo deleteIndicator(HeraIndicatorReq req) {
        try {
            // 1. 获取原指标信息用于通知
            HeraIndicatorDO originalIndicator = heraIndicatorDao.getById(req.getId());

            // 2. 检查指标是否存在关联的场景
            List<HeraSceneIndicatorDO> sceneIndicatorMappingList = heraSceneIndicatorDao
                    .queryByIndicatorId(req.getId());

            if (hasAssociatedScenes(sceneIndicatorMappingList)) {
                return buildFailedResult(ErrorInfoConstants.INDICATOR_OP_DELETE_ERROR_BY_MAPPING);
            }

            // 3. 执行删除操作
            int deleteCount = heraIndicatorDao.delete(req.getId());
            if (deleteCount > 0) {
                // 4. 发送变更通知
                try {
                    String indicatorName = originalIndicator != null ? originalIndicator.getIndicatorName() : "未知指标";
                    businessChangeNotificationService.notifyIndicatorChange(
                            req.getId(),
                            indicatorName,
                            BusinessChangeNotificationService.IndicatorOperationType.DELETE,
                            req.getOperatorAccount(),
                            "指标删除成功");
                } catch (Exception e) {
                    log.warn("发送指标删除通知失败，但指标删除成功, indicatorId={}", req.getId(), e);
                }

                return buildSuccessResult();
            } else {
                return buildFailedResult(ErrorInfoConstants.INDICATOR_HAS_BEEN_DELETED);
            }
        } catch (Exception e) {
            log.error("删除指标失败, id={}", req.getId(), e);
            return buildFailedResult(ErrorInfoConstants.SERVER_INTERNAL_ERROR);
        }
    }

    @Override
    public PageResult<HeraIndicatorResp> queryIndicators(HeraIndicatorReq req) {
        try {
            // 1. 获取用户权限的指标ID列表
            List<Long> authorizedIndicatorIds = getAuthorizedIndicatorIds(req.getOperatorAccount());
            if (hasOperatorAccount(req) && authorizedIndicatorIds.isEmpty()) {
                log.debug("用户无任何指标权限: account={}", req.getOperatorAccount());
                return buildEmptyPageResult(req);
            }

            // 2. 构建查询条件
            Cnd queryCondition = buildQueryCondition(req, authorizedIndicatorIds);

            // 3. 执行分页查询
            PageData<List<HeraIndicatorDO>> pageData = heraIndicatorDao.queryWithPagination(
                    queryCondition, req.getPageNo(), req.getPageSize());

            // 4. 转换并返回结果
            return buildPageResult(pageData, req);
        } catch (Exception e) {
            log.error("查询指标列表失败, req={}", req, e);
            return new PageResult<>();
        }
    }

    @Override
    @RequireIndicatorPermission(operation = "获取指标详情")
    public HeraIndicatorResp getIndicatorDetail(HeraIndicatorReq req) {
        try {
            HeraIndicatorDO indicator = heraIndicatorDao.getById(req.getId());
            if (indicator == null) {
                return null;
            }

            HeraIndicatorResp resp = convertToResp(indicator);

            // 计算并设置权限信息
            String ownerId = indicator.getCreator() != null ? indicator.getCreator() : "unknown";
            ResourcePermission permission = businessMonitorPermissionService
                    .calculateMetricPermission(
                            indicator.getId(), ownerId, req.getOperatorAccount());
            resp.setPermissions(permission);

            log.debug("Retrieved indicator detail with permissions for user {}, indicatorId={}",
                    req.getOperatorAccount(), req.getId());

            return resp;
        } catch (Exception e) {
            log.error("获取指标详情失败, id={}", req.getId(), e);
            return null;
        }
    }

    @Override
    public HeraIndicatorResp getIndicatorDetail(Long id) {
        // 内部调用方法，不进行权限检查
        try {
            HeraIndicatorDO indicator = heraIndicatorDao.getById(id);
            return indicator != null ? convertToResp(indicator) : null;
        } catch (Exception e) {
            log.error("获取指标详情失败, id={}", id, e);
            return null;
        }
    }

    @Override
    @RequireIndicatorPermission(operation = "启用指标")
    public boolean enableIndicator(HeraIndicatorReq req) {
        try {
            // 1. 获取原指标信息用于通知
            HeraIndicatorDO originalIndicator = heraIndicatorDao.getById(req.getId());

            // 2. 执行启用操作
            boolean success = updateIndicatorStatus(req.getId(), INDICATOR_ENABLED, "启用");

            // 3. 发送变更通知
            if (success) {
                try {
                    String indicatorName = originalIndicator != null ? originalIndicator.getIndicatorName() : "未知指标";
                    businessChangeNotificationService.notifyIndicatorChange(
                            req.getId(),
                            indicatorName,
                            BusinessChangeNotificationService.IndicatorOperationType.ENABLE,
                            req.getOperatorAccount(),
                            "指标已启用");
                } catch (Exception e) {
                    log.warn("发送指标启用通知失败，但指标启用成功, indicatorId={}", req.getId(), e);
                }
            }

            return success;
        } catch (Exception e) {
            log.error("启用指标失败, indicatorId={}", req.getId(), e);
            return false;
        }
    }

    @Override
    @RequireIndicatorPermission(operation = "禁用指标")
    public boolean disableIndicator(HeraIndicatorReq req) {
        try {
            // 1. 获取原指标信息用于通知
            HeraIndicatorDO originalIndicator = heraIndicatorDao.getById(req.getId());

            // 2. 执行禁用操作
            boolean success = updateIndicatorStatus(req.getId(), INDICATOR_DISABLED, "禁用");

            // 3. 发送变更通知
            if (success) {
                try {
                    String indicatorName = originalIndicator != null ? originalIndicator.getIndicatorName() : "未知指标";
                    businessChangeNotificationService.notifyIndicatorChange(
                            req.getId(),
                            indicatorName,
                            BusinessChangeNotificationService.IndicatorOperationType.DISABLE,
                            req.getOperatorAccount(),
                            "指标已禁用");
                } catch (Exception e) {
                    log.warn("发送指标禁用通知失败，但指标禁用成功, indicatorId={}", req.getId(), e);
                }
            }

            return success;
        } catch (Exception e) {
            log.error("禁用指标失败, indicatorId={}", req.getId(), e);
            return false;
        }
    }

    @Override
    public HeraIndicatorResp getIndicatorByName(String indicatorName) {
        // 公共方法，不进行权限检查
        try {
            HeraIndicatorDO indicator = heraIndicatorDao.queryByName(indicatorName);
            return indicator != null ? convertToResp(indicator) : null;
        } catch (Exception e) {
            log.error("根据指标名称查询指标失败, indicatorName={}", indicatorName, e);
            return null;
        }
    }

    @Override
    public HeraIndicatorResp getIndicatorByMetricName(String metricName) {
        // 公共方法，不进行权限检查
        try {
            HeraIndicatorDO indicator = heraIndicatorDao.queryByMetricName(metricName);
            return indicator != null ? convertToResp(indicator) : null;
        } catch (Exception e) {
            log.error("根据Prometheus指标名称查询指标失败, metricName={}", metricName, e);
            return null;
        }
    }

    /**
     * 根据请求构建指标DO对象
     */
    private HeraIndicatorDO buildIndicatorFromRequest(HeraIndicatorReq req) {
        HeraIndicatorDO indicator = new HeraIndicatorDO();
        BeanUtils.copyProperties(req, indicator);
        return indicator;
    }

    /**
     * 根据请求构建更新指标DO对象
     */
    private HeraIndicatorDO buildUpdateIndicatorFromRequest(HeraIndicatorReq req) {
        HeraIndicatorDO indicator = new HeraIndicatorDO();
        BeanUtils.copyProperties(req, indicator);
        indicator.setUpdatedAt(new Date());
        return indicator;
    }

    /**
     * 创建TPC权限节点（参考场景创建的模式）
     */
    private boolean createTpcPermissionNodes(HeraIndicatorReq req, Long indicatorId) {
        try {
            // 创建团队节点
            NodeVo teamNode = businessMonitorPermissionService.createTeamNode(req.getOperatorAccount());
            if (teamNode == null) {
                log.error("创建TPC团队节点失败, ownerId={}", req.getOperatorAccount());
                return false;
            }

            // 创建指标节点
            NodeVo metricNode = businessMonitorPermissionService.createMetricNode(
                    teamNode.getOutId(), indicatorId, req.getIndicatorName(), req.getOperatorAccount());
            if (metricNode == null) {
                log.error("创建TPC指标节点失败, teamOutId={}, indicatorId={}, indicatorName={}",
                        teamNode.getOutId(), indicatorId, req.getIndicatorName());
                return false;
            }

            log.info("TPC权限节点创建成功, indicatorId={}, teamNodeId={}, metricNodeId={}, indicatorName={}",
                    indicatorId, teamNode.getOutId(), metricNode.getOutId(), req.getIndicatorName());
            return true;
        } catch (Exception e) {
            log.error("创建TPC权限节点异常, indicatorId={}", indicatorId, e);
            return false;
        }
    }

    /**
     * 统一的指标状态更新方法
     */
    private boolean updateIndicatorStatus(Long indicatorId, int status, String operation) {
        try {
            HeraIndicatorDO indicator = new HeraIndicatorDO();
            indicator.setId(indicatorId);
            indicator.setIndicatorStatus(status);
            indicator.setUpdatedAt(new Date());
            return heraIndicatorDao.update(indicator) > 0;
        } catch (Exception e) {
            log.error("{}指标失败, id={}", operation, indicatorId, e);
            return false;
        }
    }

    /**
     * 检查是否存在关联场景
     */
    private boolean hasAssociatedScenes(List<HeraSceneIndicatorDO> sceneIndicatorList) {
        return sceneIndicatorList != null && !sceneIndicatorList.isEmpty();
    }

    /**
     * 检查是否有操作者账号
     */
    private boolean hasOperatorAccount(HeraIndicatorReq req) {
        return StringUtils.hasText(req.getOperatorAccount());
    }

    /**
     * 获取用户有权限的指标ID列表
     */
    private List<Long> getAuthorizedIndicatorIds(String operatorAccount) {
        if (!StringUtils.hasText(operatorAccount)) {
            return new ArrayList<>();
        }

        try {
            List<Long> authorizedIds = businessMonitorPermissionService.getMetricNodeIds(operatorAccount);
            return authorizedIds != null ? authorizedIds : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取用户指标权限失败: account={}", operatorAccount, e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建查询条件
     */
    private Cnd buildQueryCondition(HeraIndicatorReq req, List<Long> authorizedIndicatorIds) {
        Cnd cnd = Cnd.where("is_deleted", "=", NOT_DELETED);

        // 添加基本查询条件
        addBasicQueryConditions(cnd, req);

        // 权限过滤 - 只查询有权限的指标
        if (hasOperatorAccount(req) && !authorizedIndicatorIds.isEmpty()) {
            cnd.and("id", "in", authorizedIndicatorIds);
        }

        // 添加排序
        cnd.orderBy("created_at", ORDER_BY_CREATED_DESC);

        return cnd;
    }

    /**
     * 添加基本查询条件
     */
    private void addBasicQueryConditions(Cnd cnd, HeraIndicatorReq req) {
        if (StringUtils.hasText(req.getCreator())) {
            cnd.and("creator", "=", req.getCreator());
        }

        if (StringUtils.hasText(req.getIndicatorName())) {
            cnd.and("indicator_name", "like", "%" + req.getIndicatorName() + "%");
        }

        if (req.getIndicatorStatus() != null) {
            cnd.and("indicator_status", "=", req.getIndicatorStatus());
        }

        // 组织ID路径精确匹配
        if (StringUtils.hasText(req.getIdPath())) {
            cnd.and("id_path", "=", req.getIdPath());
        }

        if (req.getIndicatorType() != null) {
            cnd.and("indicator_type", "=", req.getIndicatorType());
        }
    }

    /**
     * 构建空的分页结果
     */
    private PageResult<HeraIndicatorResp> buildEmptyPageResult(HeraIndicatorReq req) {
        return new PageResult<>(List.of(), 0L, req.getPageNo(), req.getPageSize());
    }

    /**
     * 构建分页结果
     */
    private PageResult<HeraIndicatorResp> buildPageResult(PageData<List<HeraIndicatorDO>> pageData,
                                                          HeraIndicatorReq req) {
        List<HeraIndicatorDO> indicatorList = pageData.getList();
        if (indicatorList == null || indicatorList.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), pageData.getTotal(), req.getPageNo(), req.getPageSize());
        }

        // 批量计算权限 - 性能优化
        Map<Long, String> indicatorInfos = indicatorList.stream()
                .collect(Collectors.toMap(
                        HeraIndicatorDO::getId,
                        indicator -> indicator.getCreator() != null ? indicator.getCreator() : "unknown"));

        Map<Long, ResourcePermission> permissionMap = businessMonitorPermissionService
                .batchCalculateMetricPermissions(indicatorInfos, req.getOperatorAccount());

        // 转换为响应对象并设置权限
        List<HeraIndicatorResp> respList = indicatorList.stream()
                .map(indicator -> {
                    HeraIndicatorResp resp = convertToResp(indicator);
                    // 设置权限信息
                    ResourcePermission permission = permissionMap.get(indicator.getId());
                    resp.setPermissions(permission != null ? permission : ResourcePermission.viewOnlyPermission());
                    return resp;
                })
                .collect(Collectors.toList());

        log.debug("Built page result with {} indicators and calculated permissions for user {}",
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
    private HeraIndicatorResp convertToResp(HeraIndicatorDO indicator) {
        HeraIndicatorResp resp = new HeraIndicatorResp();
        BeanUtils.copyProperties(indicator, resp);
        //TODO 需要从配置中心获取特定的看板url
        if(indicator.getId() != null) {
           String dashboardUrl =  bizCustomMetricsConfig.getIndicatorDashboardUrl(indicator.getId());
           if(StringUtils.hasText(dashboardUrl)) {
               resp.setDashboardUrl(dashboardUrl);
           }
        }
        return resp;
    }

    /**
     * 获取用户的内部组织ID（用于TPC节点创建）
     */
    private Long getInnerOrgIdForUser(String account) {
        try {
            // 调用权限服务的公共方法来获取组织ID
            return businessMonitorPermissionService.getInnerOrgIdForUser(account);
        } catch (Exception e) {
            log.error("Failed to get inner org id for account: {}", account, e);
            return -1L;
        }
    }
}