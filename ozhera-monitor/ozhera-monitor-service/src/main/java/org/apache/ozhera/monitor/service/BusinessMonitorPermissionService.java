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
package org.apache.ozhera.monitor.service;

import com.xiaomi.mone.tpc.common.vo.NodeVo;
import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.ResourcePermission;
import org.apache.ozhera.monitor.bo.bizmetrics.UserPermissionInfoResp;

import java.util.List;
import java.util.Map;

/**
 * 基于TPC实现用户权限管理
 * 用户在平台创建场景和指标都会在tpc创建对应的业务空间，基于tpc业务业务空间实现权限管理
 */
public interface BusinessMonitorPermissionService {

    /**
     * 判断指定orgId是否在tpc创建过业务空间
     *
     * @param orgMappingId 根据用户组织架构idPath生成的orgId，仅限于Hera内部判重使用
     * @param account
     * @return
     */
    boolean existsTeamSpace(Long orgMappingId, String account);

    /**
     * 根据创建人创建组织架构业务空间节点
     * 用途：
     * 1.区分不同团队的业务监控信息
     * 2.基于部门级别的权限管理
     *
     * @param account cas account
     */
    NodeVo createTeamNode(String account);

    /**
     * 创建业务监控场景节点
     *
     * @param innerOrgId hera维护的内部组织架构id，仅限内部使用
     */
    NodeVo createSceneNode(Long innerOrgId, Long sceneId, String sceneName, String creatorAccount);

    /**
     * 创建业务监控指标节点
     * 该方法会在tpc场景业务空间下创建对应的指标节点
     *
     * @param innerOrgId hera维护的内部组织架构id，仅限内部使用
     */
    NodeVo createMetricNode(Long innerOrgId, Long metricId, String metricName, String creatorAccount);

    /**
     * 获取指定用户有权限的场景节点outId(对应hera场景id)
     */
    List<Long> getSceneNodeIds(String account);

    /**
     * 获取指定用户有权限的指标节点outId(对应hera指标id)
     */
    List<Long> getMetricNodeIds(String account);

    /**
     * 检查用户对节点的权限
     */
    boolean checkUserNodePermission(String userAccount, Long nodeId);

    /**
     * 场景授权
     *
     * @param sceneId
     * @param memberAccount
     * @param ownerAccount
     * @return
     */
    boolean addUserToScene(Long sceneId, String memberAccount, String ownerAccount);

    /**
     * 指标授权
     *
     * @param metricId
     * @param memberAccount
     * @param ownerAccount
     * @return
     */
    boolean addUserToMetric(Long metricId, String memberAccount, String ownerAccount);

    /**
     * 场景撤销权限
     *
     * @param sceneId       场景ID
     * @param memberAccount 成员账号
     * @param ownerAccount  拥有者账号
     * @return 是否撤销成功
     */
    boolean removeUserFromScene(Long sceneId, String memberAccount, String ownerAccount);

    /**
     * 指标撤销权限
     *
     * @param metricId      指标ID
     * @param memberAccount 成员账号
     * @param ownerAccount  拥有者账号
     * @return 是否撤销成功
     */
    boolean removeUserFromMetric(Long metricId, String memberAccount, String ownerAccount);

    /**
     * 查询某个场景的成员列表
     *
     * @param sceneId
     * @param ownerAccount
     */
    PageResult<UserPermissionInfoResp> listUserBySceneId(Long sceneId, String ownerAccount, int page, int pageSize);

    /**
     * 查询某个指标的成员列表
     *
     * @param metricId
     * @param ownerAccount
     * @return
     */
    PageResult<UserPermissionInfoResp> listUserByMetricId(Long metricId, String ownerAccount, int page, int pageSize);

    /**
     * 获取用户的内部组织ID（用于TPC节点创建）
     *
     * @param account 用户账号
     * @return 内部组织ID，如果获取失败返回-1L
     */
    Long getInnerOrgIdForUser(String account);

    /**
     * 获取订阅用户账号列表（基于tpc用户组实现）
     *
     * @return
     */
    List<String> getSubscribeUserAccounts();

    /**
     * 计算用户对场景的权限
     *
     * @param sceneId        场景ID
     * @param sceneOwnerId   场景创建者ID
     * @param currentAccount 当前用户账号
     * @return 用户权限信息
     */
   ResourcePermission calculateScenePermission(Long sceneId, String sceneOwnerId,
                                               String currentAccount);

    /**
     * 计算用户对指标的权限
     *
     * @param metricId       指标ID
     * @param metricOwnerId  指标创建者ID
     * @param currentAccount 当前用户账号
     * @return 用户权限信息
     */
   ResourcePermission calculateMetricPermission(Long metricId,
                                                                                       String metricOwnerId, String currentAccount);

    /**
     * 批量计算场景权限
     *
     * @param sceneInfos     场景信息列表(sceneId -> ownerId)
     * @param currentAccount 当前用户账号
     * @return 场景权限映射 (sceneId -> permission)
     */
    Map<Long, ResourcePermission> batchCalculateScenePermissions(
            Map<Long, String> sceneInfos, String currentAccount);

    /**
     * 批量计算指标权限
     *
     * @param metricInfos    指标信息列表(metricId -> ownerId)
     * @param currentAccount 当前用户账号
     * @return 指标权限映射 (metricId -> permission)
     */
    Map<Long, ResourcePermission> batchCalculateMetricPermissions(
            Map<Long, String> metricInfos, String currentAccount);

    /**
     * 发送事件变更消息（场景、指标的增删改事件及详情）
     *
     * @param accounts
     * @param message
     */
    void sentEventMsg(List<String> accounts, String message);
}
