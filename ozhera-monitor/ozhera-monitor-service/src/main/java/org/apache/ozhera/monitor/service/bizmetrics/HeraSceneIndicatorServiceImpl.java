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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneIndicatorReq;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraSceneIndicatorResp;
import org.apache.ozhera.monitor.bo.bizmetrics.OpFailedInfo;
import org.apache.ozhera.monitor.dao.model.HeraIndicatorDO;
import org.apache.ozhera.monitor.dao.model.HeraSceneDO;
import org.apache.ozhera.monitor.dao.model.HeraSceneIndicatorDO;
import org.apache.ozhera.monitor.dao.nutz.HeraIndicatorDao;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneDao;
import org.apache.ozhera.monitor.dao.nutz.HeraSceneIndicatorDao;
import org.apache.ozhera.monitor.service.HeraSceneIndicatorService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.nutz.dao.Cnd;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 场景-指标关联服务实现类
 */
@Slf4j
@Service
public class HeraSceneIndicatorServiceImpl implements HeraSceneIndicatorService {

    @NacosValue("${biz.metrics.dashboard.base-url:https://grafana-mione.test.mi.com/d/}")
    private String bizMetricsBaseUrl;

    @NacosValue("${biz.metrics.counter.dashboard.path:SP-c1_xXx/herabizmetriccontemp}")
    private String bizMetricsCounterDashboardPath;

    @NacosValue("${biz.metrics.gauge.dashboard.path:SP-c1_xHz/herabizmetrictmp}")
    private String bizMetricsGaugeDashboardPath;

    @Autowired
    private HeraSceneIndicatorDao heraSceneIndicatorDao;

    @Autowired
    private HeraSceneDao heraSceneDao;

    @Autowired
    private HeraIndicatorDao heraIndicatorDao;

    @Autowired
    private BusinessChangeNotificationService businessChangeNotificationService;

    @Autowired
    private BizCustomMetricsConfig bizCustomMetricsConfig;

    @Override
    public boolean isSceneIndicatorAssociated(HeraSceneIndicatorReq req) {
        try {
            if (req.getSceneId() == null || req.getIndicatorId() == null) {
                log.warn("检查场景-指标关联关系参数不完整: sceneId={}, indicatorId={}",
                        req.getSceneId(), req.getIndicatorId());
                return false;
            }

            // 构建查询条件
            Cnd cnd = Cnd.where("scene_id", "=", req.getSceneId())
                    .and("indicator_id", "=", req.getIndicatorId())
                    .and("is_deleted", "=", 0);

            // 查询是否存在匹配记录
            List<HeraSceneIndicatorDO> results = heraSceneIndicatorDao.query(cnd);

            // 如果存在记录，则表示已关联
            return results != null && !results.isEmpty();
        } catch (Exception e) {
            log.error("检查场景-指标关联关系失败, req={}", req, e);
            return false;
        }
    }

    @Override
    public OpFailedInfo createOrUpdateSceneIndicator(HeraSceneIndicatorReq req) {
        try {
            // 参数校验
            if (req.getSceneId() == null || req.getIndicatorId() == null) {
                return OpFailedInfo.builder()
                        .success(false)
                        .reason("缺少必要参数：sceneId或indicatorId")
                        .build();
            }

            // 获取场景和指标信息用于通知
            HeraSceneDO scene = heraSceneDao.getById(req.getSceneId());
            HeraIndicatorDO indicator = heraIndicatorDao.getById(req.getIndicatorId());
            String sceneName = scene != null ? scene.getSceneName() : "未知场景";
            String indicatorName = indicator != null ? indicator.getIndicatorName() : "未知指标";

            // 查询是否已存在关联
            Cnd cnd = Cnd.where("scene_id", "=", req.getSceneId())
                    .and("indicator_id", "=", req.getIndicatorId())
                    .and("is_deleted", "=", 0);

            List<HeraSceneIndicatorDO> existingRecords = heraSceneIndicatorDao.query(cnd);

            // 构造数据对象
            HeraSceneIndicatorDO sceneIndicator = new HeraSceneIndicatorDO();
            BeanUtils.copyProperties(req, sceneIndicator);

            // 根据是否存在记录决定创建或更新
            if (existingRecords == null || existingRecords.isEmpty()) {
                // 创建新记录
                HeraSceneIndicatorDO result = heraSceneIndicatorDao.insert(sceneIndicator);

                if (result != null && result.getId() != null) {
                    // 如果创建成功，则更新指标监控大盘url
                    updateDashboardUrl(req.getSceneId(), req.getIndicatorId());

                    // 发送变更通知
                    try {
                        businessChangeNotificationService.notifySceneIndicatorChange(
                                req.getSceneId(), sceneName, req.getIndicatorId(), indicatorName,
                                BusinessChangeNotificationService.SceneIndicatorOperationType.CREATE,
                                getCurrentOperatorAccount(req), "场景指标关联创建成功");
                    } catch (Exception e) {
                        log.warn("发送场景指标关联创建通知失败，但关联创建成功, sceneId={}, indicatorId={}",
                                req.getSceneId(), req.getIndicatorId(), e);
                    }

                    return OpFailedInfo.builder()
                            .success(true)
                            .data(result.getId())
                            .reason("created") // 将操作类型放在reason字段
                            .build();
                } else {
                    return OpFailedInfo.builder()
                            .success(false)
                            .reason("创建场景-指标关联失败-1")
                            .build();
                }
            } else {
                // 更新已有记录
                HeraSceneIndicatorDO existingRecord = existingRecords.get(0);

                // 保留ID，更新其他属性
                sceneIndicator.setId(existingRecord.getId());
                int result = heraSceneIndicatorDao.update(sceneIndicator);
                if (result > 0) {
                    // 发送变更通知
                    try {
                        businessChangeNotificationService.notifySceneIndicatorChange(
                                req.getSceneId(), sceneName, req.getIndicatorId(), indicatorName,
                                BusinessChangeNotificationService.SceneIndicatorOperationType.UPDATE,
                                getCurrentOperatorAccount(req), "场景指标关联更新成功");
                    } catch (Exception e) {
                        log.warn("发送场景指标关联更新通知失败，但关联更新成功, sceneId={}, indicatorId={}",
                                req.getSceneId(), req.getIndicatorId(), e);
                    }

                    return OpFailedInfo.builder()
                            .success(true)
                            .data(existingRecord.getId())
                            .reason("updated") // 将操作类型放在reason字段
                            .build();
                } else {
                    return OpFailedInfo.builder()
                            .success(false)
                            .reason("更新场景-指标关联失败-2")
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("创建或更新场景-指标关联失败, req={}", req, e);
            return OpFailedInfo.builder()
                    .success(false)
                    .reason("系统异常：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean updateSceneIndicator(HeraSceneIndicatorReq req) {
        try {
            HeraSceneIndicatorDO sceneIndicator = new HeraSceneIndicatorDO();
            BeanUtils.copyProperties(req, sceneIndicator);
            sceneIndicator.setId(req.getId());
            return heraSceneIndicatorDao.update(sceneIndicator) > 0;
        } catch (Exception e) {
            log.error("更新场景-指标关联失败, req={}", req, e);
            return false;
        }
    }

    @Override
    public boolean deleteSceneIndicator(Long id) {
        try {
            // 获取关联信息用于通知
            HeraSceneIndicatorDO sceneIndicator = heraSceneIndicatorDao.getById(id);
            if (sceneIndicator == null) {
                log.warn("要删除的场景指标关联不存在, id={}", id);
                return false;
            }

            HeraSceneDO scene = heraSceneDao.getById(sceneIndicator.getSceneId());
            HeraIndicatorDO indicator = heraIndicatorDao.getById(sceneIndicator.getIndicatorId());
            String sceneName = scene != null ? scene.getSceneName() : "未知场景";
            String indicatorName = indicator != null ? indicator.getIndicatorName() : "未知指标";

            // 执行删除
            boolean deleteSuccess = heraSceneIndicatorDao.delete(id) > 0;

            if (deleteSuccess) {
                // 发送变更通知
                try {
                    businessChangeNotificationService.notifySceneIndicatorChange(
                            sceneIndicator.getSceneId(), sceneName, sceneIndicator.getIndicatorId(), indicatorName,
                            BusinessChangeNotificationService.SceneIndicatorOperationType.DELETE,
                            "system", "场景指标关联删除成功");
                } catch (Exception e) {
                    log.warn("发送场景指标关联删除通知失败，但关联删除成功, sceneId={}, indicatorId={}",
                            sceneIndicator.getSceneId(), sceneIndicator.getIndicatorId(), e);
                }
            }

            return deleteSuccess;
        } catch (Exception e) {
            log.error("删除场景-指标关联失败, id={}", id, e);
            return false;
        }
    }

    @Override
    public PageResult<HeraSceneIndicatorResp> querySceneIndicators(HeraSceneIndicatorReq req) {
        try {
            // 构建查询条件
            Cnd cnd = Cnd.where("is_deleted", "=", 0);

            if (req.getSceneId() != null) {
                cnd.and("scene_id", "=", req.getSceneId());
            }
            if (req.getIndicatorId() != null) {
                cnd.and("indicator_id", "=", req.getIndicatorId());
            }

            // 执行分页查询
            PageData<List<HeraSceneIndicatorDO>> pageData = heraSceneIndicatorDao.queryWithPagination(
                    cnd, req.getPageNo(), req.getPageSize());

            // 转换结果
            List<HeraSceneIndicatorResp> respList = pageData.getList().stream()
                    .map(this::convertToResp)
                    .collect(Collectors.toList());

            // 构建分页结果
            return new PageResult<>(
                    respList,
                    pageData.getTotal(),
                    req.getPageNo(),
                    req.getPageSize());
        } catch (Exception e) {
            log.error("查询场景-指标关联列表失败, req={}", req, e);
            return new PageResult<>();
        }
    }

    @Override
    public HeraSceneIndicatorResp getSceneIndicatorDetail(Long id) {
        try {
            HeraSceneIndicatorDO sceneIndicator = heraSceneIndicatorDao.getById(id);
            return sceneIndicator != null ? convertToResp(sceneIndicator) : null;
        } catch (Exception e) {
            log.error("获取场景-指标关联详情失败, id={}", id, e);
            return null;
        }
    }

    @Override
    public List<HeraSceneIndicatorResp> queryBySceneId(Long sceneId) {
        try {
            List<HeraSceneIndicatorDO> sceneIndicators = heraSceneIndicatorDao.queryBySceneId(sceneId);
            return sceneIndicators.stream()
                    .map(this::convertToResp)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据场景ID查询场景-指标关联列表失败, sceneId={}", sceneId, e);
            return List.of();
        }
    }

    @Override
    public List<HeraSceneIndicatorResp> queryByIndicatorId(Long indicatorId) {
        try {
            List<HeraSceneIndicatorDO> sceneIndicators = heraSceneIndicatorDao.queryByIndicatorId(indicatorId);
            return sceneIndicators.stream()
                    .map(this::convertToResp)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据指标ID查询场景-指标关联列表失败, indicatorId={}", indicatorId, e);
            return List.of();
        }
    }

    /**
     * 将DO对象转换为响应对象
     */
    private HeraSceneIndicatorResp convertToResp(HeraSceneIndicatorDO sceneIndicator) {
        HeraSceneIndicatorResp resp = new HeraSceneIndicatorResp();
        BeanUtils.copyProperties(sceneIndicator, resp);

        // 获取场景信息
        if (sceneIndicator.getSceneId() != null) {
            HeraSceneDO scene = heraSceneDao.getById(sceneIndicator.getSceneId());
            if (scene != null) {
                resp.setSceneName(scene.getSceneName());
            }
        }

        // 获取指标信息
        if (sceneIndicator.getIndicatorId() != null) {
            HeraIndicatorDO indicator = heraIndicatorDao.getById(sceneIndicator.getIndicatorId());
            if (indicator != null) {
                resp.setIndicatorName(indicator.getIndicatorName());
                resp.setIndicatorType(indicator.getIndicatorType());
                resp.setDashboardUrl(indicator.getDashboardUrl());
            }
        }

        // 设置时间
        if (sceneIndicator.getCreatedAt() != null) {
            resp.setCreateTime(sceneIndicator.getCreatedAt().getTime());
        }
        if (sceneIndicator.getUpdatedAt() != null) {
            resp.setUpdateTime(sceneIndicator.getUpdatedAt().getTime());
        }

        return resp;
    }

    private void updateDashboardUrl(long sceneId, long indicatorId) {
        if (0 > indicatorId) {
            return;
        }

        boolean hasIndicatorConfig = bizCustomMetricsConfig.hasIndicatorConfig(indicatorId);
        if(hasIndicatorConfig){
            // 如果存在自定义指标看板，则不更新指标看板url数据
            return;
        }

        HeraIndicatorDO heraIndicatorDO = heraIndicatorDao.getById(indicatorId);
        if (Objects.isNull(heraIndicatorDO)) {
            log.warn("指标[{}]信息不存在", indicatorId);
            return;
        }
        // String dashboardUrl = String.format(
        // "%s?orgId=1&from=now-6h&to=now&var-sceneId=%d&var-businessMetric=%d&var-businessMetricName=%s&var-dashboardTitle=%s&kiosk",
        // grafanaBaseUrl, sceneId, indicatorId, heraIndicatorDO.getIndicatorName(),
        // heraIndicatorDO.getIndicatorName());

        String baseUrl = "";
        if (heraIndicatorDO.getIndicatorType() == 0) {
            baseUrl = bizMetricsBaseUrl + bizMetricsCounterDashboardPath;
        } else if (heraIndicatorDO.getIndicatorType() == 1) {
            baseUrl = bizMetricsBaseUrl + bizMetricsGaugeDashboardPath;
        } else {
            log.error("the_metric_type_is_incorrect,indicatorId:{}", indicatorId);
        }

        String dashboardUrl = String.format("%s?orgId=1&from=now-6h&to=now&var-businessMetric=%d&var-businessMetricName=%s&var-dashboardTitle=%s&kiosk",
                baseUrl,
                indicatorId,
                heraIndicatorDO.getIndicatorName(),
                heraIndicatorDO.getIndicatorName());

        heraIndicatorDO.setDashboardUrl(dashboardUrl);
        heraIndicatorDao.update(heraIndicatorDO);
        log.info("Grafana dashboardUrl: {}", dashboardUrl);
    }

    /**
     * 获取当前操作者账号，支持从请求中获取或使用默认值
     */
    private String getCurrentOperatorAccount(HeraSceneIndicatorReq req) {
        // 这里可以从上下文或请求中获取操作者账号
        // 暂时返回一个默认值，实际使用时需要根据项目的用户体系来获取
        return "system";
    }

}