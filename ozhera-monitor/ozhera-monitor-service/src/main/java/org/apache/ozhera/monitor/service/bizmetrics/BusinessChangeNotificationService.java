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

import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.service.BusinessMonitorPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 业务变更通知服务
 * 负责在场景和指标发生增删改操作时，向订阅用户发送变更通知
 */
@Slf4j
@Service
public class BusinessChangeNotificationService {

    @Autowired
    private BusinessMonitorPermissionService businessMonitorPermissionService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 场景操作类型枚举
     */
    public enum SceneOperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除"),
        ENABLE("启用"),
        DISABLE("禁用");

        private final String description;

        SceneOperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 指标操作类型枚举
     */
    public enum IndicatorOperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除"),
        ENABLE("启用"),
        DISABLE("禁用");

        private final String description;

        IndicatorOperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 场景指标关联操作类型枚举
     */
    public enum SceneIndicatorOperationType {
        CREATE("创建关联"),
        UPDATE("更新关联"),
        DELETE("删除关联");

        private final String description;

        SceneIndicatorOperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 发送场景变更通知
     *
     * @param sceneId         场景ID
     * @param sceneName       场景名称
     * @param operationType   操作类型
     * @param operatorAccount 操作者账号
     * @param remark          备注信息（可选）
     */
    public void notifySceneChange(Long sceneId, String sceneName, SceneOperationType operationType,
            String operatorAccount, String remark) {
        try {
            // 获取订阅用户列表
            List<String> subscribeAccounts = businessMonitorPermissionService.getSubscribeUserAccounts();

            if (subscribeAccounts == null || subscribeAccounts.isEmpty()) {
                log.debug("没有订阅用户，跳过场景变更通知");
                return;
            }

            // 构建消息内容
            String message = buildSceneChangeMessage(sceneId, sceneName, operationType, operatorAccount, remark);

            // 发送通知
            businessMonitorPermissionService.sentEventMsg(subscribeAccounts, message);

            log.info("场景变更通知发送成功 - 场景ID: {}, 操作: {}, 操作者: {}, 接收人数: {}",
                    sceneId, operationType.getDescription(), operatorAccount, subscribeAccounts.size());

        } catch (Exception e) {
            log.error("发送场景变更通知失败 - 场景ID: {}, 操作: {}, 操作者: {}",
                    sceneId, operationType.getDescription(), operatorAccount, e);
        }
    }

    /**
     * 发送指标变更通知
     *
     * @param indicatorId     指标ID
     * @param indicatorName   指标名称
     * @param operationType   操作类型
     * @param operatorAccount 操作者账号
     * @param remark          备注信息（可选）
     */
    public void notifyIndicatorChange(Long indicatorId, String indicatorName, IndicatorOperationType operationType,
            String operatorAccount, String remark) {
        try {
            // 获取订阅用户列表
            List<String> subscribeAccounts = businessMonitorPermissionService.getSubscribeUserAccounts();

            if (subscribeAccounts == null || subscribeAccounts.isEmpty()) {
                log.debug("没有订阅用户，跳过指标变更通知");
                return;
            }

            // 构建消息内容
            String message = buildIndicatorChangeMessage(indicatorId, indicatorName, operationType, operatorAccount,
                    remark);

            // 发送通知
            businessMonitorPermissionService.sentEventMsg(subscribeAccounts, message);

            log.info("指标变更通知发送成功 - 指标ID: {}, 操作: {}, 操作者: {}, 接收人数: {}",
                    indicatorId, operationType.getDescription(), operatorAccount, subscribeAccounts.size());

        } catch (Exception e) {
            log.error("发送指标变更通知失败 - 指标ID: {}, 操作: {}, 操作者: {}",
                    indicatorId, operationType.getDescription(), operatorAccount, e);
        }
    }

    /**
     * 发送场景指标关联变更通知
     *
     * @param sceneId         场景ID
     * @param sceneName       场景名称
     * @param indicatorId     指标ID
     * @param indicatorName   指标名称
     * @param operationType   操作类型
     * @param operatorAccount 操作者账号
     * @param remark          备注信息（可选）
     */
    public void notifySceneIndicatorChange(Long sceneId, String sceneName, Long indicatorId, String indicatorName,
            SceneIndicatorOperationType operationType, String operatorAccount, String remark) {
        try {
            // 获取订阅用户列表
            List<String> subscribeAccounts = businessMonitorPermissionService.getSubscribeUserAccounts();

            if (subscribeAccounts == null || subscribeAccounts.isEmpty()) {
                log.debug("没有订阅用户，跳过场景指标关联变更通知");
                return;
            }

            // 构建消息内容
            String message = buildSceneIndicatorChangeMessage(sceneId, sceneName, indicatorId, indicatorName,
                    operationType, operatorAccount, remark);

            // 发送通知
            businessMonitorPermissionService.sentEventMsg(subscribeAccounts, message);

            log.info("场景指标关联变更通知发送成功 - 场景ID: {}, 指标ID: {}, 操作: {}, 操作者: {}, 接收人数: {}",
                    sceneId, indicatorId, operationType.getDescription(), operatorAccount, subscribeAccounts.size());

        } catch (Exception e) {
            log.error("发送场景指标关联变更通知失败 - 场景ID: {}, 指标ID: {}, 操作: {}, 操作者: {}",
                    sceneId, indicatorId, operationType.getDescription(), operatorAccount, e);
        }
    }

    /**
     * 构建场景变更消息
     */
    private String buildSceneChangeMessage(Long sceneId, String sceneName, SceneOperationType operationType,
            String operatorAccount, String remark) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("【业务监控场景变更通知】\n");
        messageBuilder.append("操作类型: ").append(operationType.getDescription()).append("\n");
        messageBuilder.append("场景ID: ").append(sceneId).append("\n");
        messageBuilder.append("场景名称: ").append(sceneName != null ? sceneName : "未知").append("\n");
        messageBuilder.append("操作者: ").append(operatorAccount).append("\n");
        messageBuilder.append("操作时间: ").append(DATE_FORMAT.format(new Date())).append("\n");

        if (remark != null && !remark.trim().isEmpty()) {
            messageBuilder.append("备注: ").append(remark).append("\n");
        }

        return messageBuilder.toString();
    }

    /**
     * 构建指标变更消息
     */
    private String buildIndicatorChangeMessage(Long indicatorId, String indicatorName,
            IndicatorOperationType operationType,
            String operatorAccount, String remark) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("【业务监控指标变更通知】\n");
        messageBuilder.append("操作类型: ").append(operationType.getDescription()).append("\n");
        messageBuilder.append("指标ID: ").append(indicatorId).append("\n");
        messageBuilder.append("指标名称: ").append(indicatorName != null ? indicatorName : "未知").append("\n");
        messageBuilder.append("操作者: ").append(operatorAccount).append("\n");
        messageBuilder.append("操作时间: ").append(DATE_FORMAT.format(new Date())).append("\n");

        if (remark != null && !remark.trim().isEmpty()) {
            messageBuilder.append("备注: ").append(remark).append("\n");
        }

        return messageBuilder.toString();
    }

    /**
     * 构建场景指标关联变更消息
     */
    private String buildSceneIndicatorChangeMessage(Long sceneId, String sceneName, Long indicatorId,
            String indicatorName,
            SceneIndicatorOperationType operationType, String operatorAccount, String remark) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("【业务监控场景指标关联变更通知】\n");
        messageBuilder.append("操作类型: ").append(operationType.getDescription()).append("\n");
        messageBuilder.append("场景ID: ").append(sceneId).append(", 场景名称: ").append(sceneName != null ? sceneName : "未知")
                .append("\n");
        messageBuilder.append("指标ID: ").append(indicatorId).append(", 指标名称: ")
                .append(indicatorName != null ? indicatorName : "未知").append("\n");
        messageBuilder.append("操作者: ").append(operatorAccount).append("\n");
        messageBuilder.append("操作时间: ").append(DATE_FORMAT.format(new Date())).append("\n");

        if (remark != null && !remark.trim().isEmpty()) {
            messageBuilder.append("备注: ").append(remark).append("\n");
        }

        return messageBuilder.toString();
    }
}