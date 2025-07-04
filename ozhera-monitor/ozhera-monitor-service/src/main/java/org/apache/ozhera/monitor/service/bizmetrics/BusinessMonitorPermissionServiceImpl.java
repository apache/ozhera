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
import com.google.common.collect.Maps;
import com.xiaomi.mone.tpc.api.service.NodeFacade;
import com.xiaomi.mone.tpc.api.service.NodeUserFacade;
import com.xiaomi.mone.tpc.api.service.UserFacade;
import com.xiaomi.mone.tpc.api.service.UserGroupFacade;
import com.xiaomi.mone.tpc.api.service.UserOrgFacade;
import com.xiaomi.mone.tpc.common.enums.NodeTypeEnum;
import com.xiaomi.mone.tpc.common.enums.NodeUserRelTypeEnum;
import com.xiaomi.mone.tpc.common.enums.OutIdTypeEnum;
import com.xiaomi.mone.tpc.common.enums.UserTypeEnum;
import com.xiaomi.mone.tpc.common.param.NodeAddParam;
import com.xiaomi.mone.tpc.common.param.NodeQryParam;
import com.xiaomi.mone.tpc.common.param.NodeUserAddParam;
import com.xiaomi.mone.tpc.common.param.NodeUserDeleteParam;
import com.xiaomi.mone.tpc.common.param.NodeUserQryParam;
import com.xiaomi.mone.tpc.common.param.NullParam;
import com.xiaomi.mone.tpc.common.param.OrgInfoParam;
import com.xiaomi.mone.tpc.common.param.UserGroupMemberQryParam;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.NodeVo;
import com.xiaomi.mone.tpc.common.vo.OrgInfoVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.common.vo.UserGroupRelVo;
import com.xiaomi.youpin.infra.rpc.Result;
import com.xiaomi.youpin.infra.rpc.errors.GeneralCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.ozhera.monitor.bo.PageResult;
import org.apache.ozhera.monitor.bo.bizmetrics.ResourcePermission;
import org.apache.ozhera.monitor.bo.bizmetrics.UserPermissionInfoResp;
import org.apache.ozhera.monitor.dao.model.TpcOutIdOrgMappingDO;
import org.apache.ozhera.monitor.dao.nutz.TpcOutIdOrgMappingDao;
import org.apache.ozhera.monitor.enums.UserRole;
import org.apache.ozhera.monitor.service.BusinessMonitorPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class BusinessMonitorPermissionServiceImpl implements BusinessMonitorPermissionService {

    @Reference(registry = "registryConfig", check = false, version = "1.0", interfaceClass = UserOrgFacade.class, group = "${dubbo.group.mifaass}")
    private UserOrgFacade userOrgFacade;

    @Reference(registry = "registryConfig", check = false, version = "1.0", interfaceClass = UserFacade.class, group = "${dubbo.group.mifaass}")
    private UserFacade userFacade;

    @Reference(registry = "registryConfig", check = false, version = "1.0", interfaceClass = NodeFacade.class, group = "${dubbo.group.mifaass}")
    private NodeFacade nodeFacade;

    @Reference(registry = "registryConfig", check = false, version = "1.0", interfaceClass = NodeUserFacade.class, group = "${dubbo.group.mifaass}")
    private NodeUserFacade nodeUserFacade;

    @Reference(registry = "registryConfig", check = false, version = "1.0", interfaceClass = UserGroupFacade.class, group = "${dubbo.group.mifaass}")
    private UserGroupFacade userGroupFacade;

    @Autowired
    private TpcOutIdOrgMappingDao tpcOutIdOrgMappingDao;

    @NacosValue("${business.monitor.tpc.root.node.id:373753}")
    private Long TPC_ROOT_NODE_ID;

    @NacosValue("${business.monitor.tpc.node.page.size:3}")
    private Integer TPC_NODE_PAGE_SIZE;

    @NacosValue("${business.monitor.tpc.super.account:jiangyanze}")
    private String SUPER_MGR_ACCOUNT;

    @NacosValue("${business.monitor.tpc.subscribe.group.id:90005}")
    private Long MGR_GROUP_ID;

    private static final Integer TEAM_TYPE_CODE = OutIdTypeEnum.HERA_BIZ_TEAM.getCode();
    private static final Integer SCENE_TYPE_CODE = OutIdTypeEnum.HERA_BIZ_SCENE.getCode();
    private static final Integer METRIC_TYPE_CODE = OutIdTypeEnum.HERA_BIZ_METRIC.getCode();
    private static final Integer USER_ACCOUNT_TYPE = UserTypeEnum.CAS_TYPE.getCode();

    private NodeVo getTeamNode(Long outId, String account) {
        return getNodeVoByOutId(TEAM_TYPE_CODE, outId, account);
    }

    private NodeVo getSceneNode(Long outId, String account) {
        return getNodeVoByOutId(SCENE_TYPE_CODE, outId, account);
    }

    private NodeVo getMetricNode(Long outId, String account) {
        return getNodeVoByOutId(METRIC_TYPE_CODE, outId, account);
    }

    private NodeVo getNodeVoByOutId(Integer outIdType, Long outId, String account) {
        NodeQryParam param = new NodeQryParam();
        param.setOutId(outId);
        param.setAccount(account);
        param.setUserType(UserTypeEnum.CAS_TYPE.getCode());
        param.setOutIdType(outIdType);
        Result<NodeVo> result = nodeFacade.getNodeByOutId(param);
        if (isSuccess(result)) {
            return result.getData();
        }
        return null;
    }

    @Override
    public boolean existsTeamSpace(Long orgMappingId, String account) {
        NodeQryParam param = new NodeQryParam();
        param.setOutId(orgMappingId);
        param.setAccount(account);
        param.setUserType(UserTypeEnum.CAS_TYPE.getCode());
        param.setOutIdType(SCENE_TYPE_CODE);
        Result result = nodeFacade.exists(param);
        log.debug("existsTeamSpace result for orgMappingId: {}, account: {}, result: {}", orgMappingId, account,
                result);
        return isSuccess(result);
    }

    @Override
    public NodeVo createTeamNode(String account) {
        if (account == null || account.trim().isEmpty()) {
            log.warn("Account is null or empty for createTeamNode");
            throw new IllegalArgumentException("Account cannot be null or empty");
        }

        log.info("Creating team node for account: {}", account);

        Map<String, String> userInfo = null;
        try {
            // 1. 获取用户组织架构信息
            userInfo = getUserInfo(account);
            if (userInfo.isEmpty()) {
                log.warn("No organization info found for account: {}", account);
                throw new RuntimeException("Invalid account: " + account);
            }

            String namePath = userInfo.get("namePath");
            String idPath = userInfo.get("idPath");

            if (idPath == null || idPath.trim().isEmpty()) {
                log.warn("IdPath is empty for account: {}, userInfo: {}", account, userInfo);
                throw new RuntimeException("User organization path is empty for account: " + account);
            }

            log.debug("User org info - account: {}, namePath: {}, idPath: {}", account, namePath, idPath);

            // 2. 存储组织架构idPath并使用数据库自增id作为outid与tpc联动
            TpcOutIdOrgMappingDO outIdOrgMapping = new TpcOutIdOrgMappingDO();
            outIdOrgMapping.setOutType(TEAM_TYPE_CODE);
            outIdOrgMapping.setIdPath(idPath);

            try {
                outIdOrgMapping = tpcOutIdOrgMappingDao.insert(outIdOrgMapping);
                if (outIdOrgMapping == null) {
                    log.error("Failed to insert org mapping - returned null for account: {}, idPath: {}", account,
                            idPath);
                    throw new RuntimeException("Failed to create organization mapping");
                }
                log.debug("Created org mapping - id: {}, idPath: {}, outType: {}",
                        outIdOrgMapping.getId(), outIdOrgMapping.getIdPath(), outIdOrgMapping.getOutType());
            } catch (Exception e) {
                log.error("Failed to insert org mapping for account: {}, idPath: {}", account, idPath, e);
                throw new RuntimeException("Failed to create organization mapping: " + e.getMessage(), e);
            }

            // 3. 检查tpc是否已经存在对应的组织架构业务空间
            NodeVo teamNode = getTeamNode(outIdOrgMapping.getId(), account);
            if (teamNode != null) {
                log.info("Team node already exists for account: {}, nodeId: {}, nodeName: {}",
                        account, teamNode.getId(), teamNode.getNodeName());
                return teamNode;
            }

            log.debug("TPC node not found, creating new node for account: {}, mappingId: {}",
                    account, outIdOrgMapping.getId());

            // 4. 如果不存在，创建新的组织架构业务节点
            String nodeName = extractLastPartOfPath(namePath);
            log.debug("Extracted node name: {} from namePath: {}", nodeName, namePath);

            NodeAddParam param = new NodeAddParam();
            param.setNodeName(nodeName);
            param.setOutId(outIdOrgMapping.getId());
            param.setType(NodeTypeEnum.PRO_SUB_GROUP.getCode());
            param.setParentNodeId(TPC_ROOT_NODE_ID);
            param.setOutIdType(TEAM_TYPE_CODE);
            param.setAccount(account);
            param.setUserType(USER_ACCOUNT_TYPE);

            OrgInfoParam orgInfoParam = new OrgInfoParam();
            orgInfoParam.setIdPath(idPath);
            orgInfoParam.setNamePath(namePath);
            param.setOrgParam(orgInfoParam);

            log.debug("Creating TPC node - nodeName: {}, outId: {}, parentNodeId: {}, account: {}",
                    nodeName, outIdOrgMapping.getId(), TPC_ROOT_NODE_ID, account);

            Result<NodeVo> result = nodeFacade.add(param);
            if (isSuccess(result) && result.getData() != null) {
                NodeVo createdNode = result.getData();
                log.info("Successfully created team node - ID: {}, Name: {}, OutId: {}, Account: {}",
                        createdNode.getId(), createdNode.getNodeName(), createdNode.getOutId(), account);
                return createdNode;
            } else {
                String errorMsg = result != null ? result.getMessage() : "Unknown error";
                log.error("Failed to create TPC node - account: {}, mappingId: {}, error: {}",
                        account, outIdOrgMapping.getId(), errorMsg);
                throw new RuntimeException("Failed to create TPC node: " + errorMsg);
            }

        } catch (IllegalArgumentException e) {
            // 参数验证异常，直接抛出
            log.warn("Invalid argument for createTeamNode - account: {}, error: {}", account, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            // 业务异常，记录日志后抛出
            log.error("Business error creating team node for account: {}, userInfo: {}, error: {}",
                    account, userInfo, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 未知异常，记录详细日志后包装抛出
            log.error("Unexpected error creating team node for account: {}, userInfo: {}",
                    account, userInfo, e);
            throw new RuntimeException("Unexpected error occurred while creating team node: " + e.getMessage(), e);
        }
    }

    @Override
    public NodeVo createSceneNode(Long innerOrgId, Long sceneId, String sceneName,
            String creatorAccount) {
        if (innerOrgId == null || sceneId == null) {
            throw new IllegalArgumentException("innerOrgId and sceneId cannot be null");
        }

        if (sceneName == null || sceneName.trim().isEmpty()) {
            throw new IllegalArgumentException("sceneName cannot be null or empty");
        }

        if (creatorAccount == null || creatorAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("creatorAccount cannot be null or empty");
        }

        log.info("Creating scene node: parentNodeId={}, sceneId={}, sceneName={}, creatorAccount={}",
                innerOrgId, sceneId, sceneName, creatorAccount);

        try {
            // 检查场景节点是否已存在
            try {
                NodeVo existingNode = getSceneNode(sceneId, creatorAccount);
                if (existingNode != null) {
                    log.info("Scene node already exists: {}", existingNode.getId());
                    return existingNode;
                }
            } catch (Exception e) {
                // 节点不存在，继续创建
                log.debug("Scene node does not exist, creating new one");
            }

            // 创建场景节点
            NodeAddParam param = new NodeAddParam();
            param.setParentOutId(innerOrgId);
            param.setParentOutIdType(TEAM_TYPE_CODE);
            param.setNodeName(sceneName.trim());
            param.setOutId(sceneId);
            param.setOutIdType(SCENE_TYPE_CODE);
            param.setType(NodeTypeEnum.PRO_SUB_GROUP.getCode());
            param.setAccount(creatorAccount);
            param.setUserType(USER_ACCOUNT_TYPE);

            Result<NodeVo> result = nodeFacade.add(param);
            if (isSuccess(result)) {
                NodeVo createdNode = result.getData();
                log.info("Successfully created scene node: ID={}, Name={}, OutId={}",
                        createdNode.getId(), createdNode.getNodeName(), createdNode.getOutId());
                return createdNode;
            } else {
                throw new RuntimeException("Failed to create scene node: " + result.getCode());
            }
        } catch (Exception e) {
            log.error("Failed to create scene node: parentNodeId={}, sceneId={}, sceneName={}",
                    innerOrgId, sceneId, sceneName, e);
            throw new RuntimeException("Failed to create scene node: " + e.getMessage(), e);
        }
    }

    @Override
    public NodeVo createMetricNode(Long innerOrgId, Long metricId, String metricName,
            String creatorAccount) {
        if (innerOrgId == null || metricId == null) {
            throw new IllegalArgumentException("innerOrgId and metricId cannot be null");
        }

        if (metricName == null || metricName.trim().isEmpty()) {
            throw new IllegalArgumentException("metricName cannot be null or empty");
        }

        if (creatorAccount == null || creatorAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("creatorAccount cannot be null or empty");
        }

        log.info("Creating metric node: innerOrgId={}, metricId={}, metricName={}, creatorAccount={}",
                innerOrgId, metricId, metricName, creatorAccount);

        try {
            // 检查指标节点是否已存在
            try {
                NodeVo existingNode = getMetricNode(metricId, creatorAccount);
                if (existingNode != null) {
                    log.info("Metric node already exists: {}", existingNode.getId());
                    return existingNode;
                }
            } catch (Exception e) {
                // 节点不存在，继续创建
                log.debug("Metric node does not exist, creating new one");
            }

            // 创建指标节点
            NodeAddParam param = new NodeAddParam();
            param.setParentOutId(innerOrgId);
            param.setParentOutIdType(TEAM_TYPE_CODE);
            param.setNodeName(metricName.trim());
            param.setOutId(metricId);
            param.setOutIdType(METRIC_TYPE_CODE);
            param.setType(NodeTypeEnum.PRO_SUB_GROUP.getCode());
            param.setAccount(creatorAccount);
            param.setUserType(USER_ACCOUNT_TYPE);

            Result<NodeVo> result = nodeFacade.add(param);
            if (isSuccess(result)) {
                NodeVo createdNode = result.getData();
                log.info("Successfully created metric node: ID={}, Name={}, OutId={}, ParentId={}",
                        createdNode.getId(), createdNode.getNodeName(), createdNode.getOutId(), innerOrgId);
                return createdNode;
            } else {
                throw new RuntimeException("Failed to create metric node: " + result.getCode());
            }
        } catch (Exception e) {
            log.error("Failed to create metric node: innerOrgId={}, metricId={}, metricName={}",
                    innerOrgId, metricId, metricName, e);
            throw new RuntimeException("Failed to create metric node: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Long> getSceneNodeIds(String account) {
        Map<Integer, List<Long>> resultMap = getNodeIdsByType(account);
        return Objects.isNull(resultMap.get(SCENE_TYPE_CODE)) ? new ArrayList<>() : resultMap.get(SCENE_TYPE_CODE);
    }

    @Override
    public List<Long> getMetricNodeIds(String account) {
        Map<Integer, List<Long>> resultMap = getNodeIdsByType(account);
        return Objects.isNull(resultMap.get(METRIC_TYPE_CODE)) ? new ArrayList<>() : resultMap.get(METRIC_TYPE_CODE);
    }

    /**
     * 通用方法：根据账号和节点类型获取节点ID列表
     *
     * @param account 用户账号
     * @return outIdType&节点ID列表
     */
    private Map<Integer, List<Long>> getNodeIdsByType(String account) {
        Map<Integer, List<Long>> resultMap = new HashMap<>();

        if (account == null || account.trim().isEmpty()) {
            log.warn("Account is null or empty");
            return resultMap;
        }

        try {
            Long outOrgId = getInnerOrgId(account);
            if (outOrgId == -1L) {
                log.warn("Failed to get inner org ID for account: {}, returning empty map",
                        account);
                return resultMap;
            }

            NodeQryParam param = new NodeQryParam();
            param.setAccount(account);
            param.setUserType(USER_ACCOUNT_TYPE);
            // param.setParentOutId(outOrgId);
            // param.setParentOutIdType(TEAM_TYPE_CODE);
            param.setMyNode(true);// myNode=true自己是节点成员的子节点列表
            param.setPageSize(TPC_NODE_PAGE_SIZE);
            param.setPager(Boolean.TRUE);

            int page = 1;
            int totalNodes = 0;
            // 添加最大页数限制，防止死循环
            int consecutiveEmptyPages = 0;
            int maxPageSize = 1000;

            while (page <= maxPageSize) {
                param.setPage(page);
                Result<PageDataVo<NodeVo>> result = nodeFacade.list(param);

                if (!isSuccess(result)) {
                    log.warn("Failed to get nodes for page {}, account: {}, result: {}",
                            page, account, result != null ? result.getMessage() : "null");
                    break;
                }

                // 检查返回的数据结构
                if (result.getData() == null) {
                    log.debug("No data returned for page {} for account: {}", page, account);
                    consecutiveEmptyPages++;
                    if (consecutiveEmptyPages >= 3) {
                        log.debug("Reached maximum consecutive empty pages for account: {}", account);
                        break;
                    }
                    page++;
                    continue;
                }

                List<NodeVo> nodeList = result.getData().getList();
                if (nodeList == null || nodeList.isEmpty()) {
                    log.debug("Empty node list for page {} for account: {}", page, account);
                    consecutiveEmptyPages++;
                    if (consecutiveEmptyPages >= 3) {
                        log.debug("Reached maximum consecutive empty pages for account: {}", account);
                        break;
                    }
                    page++;
                    continue;
                }

                // 重置连续空页面计数器
                consecutiveEmptyPages = 0;

                // 按NodeVo中的outIdType进行分组
                for (NodeVo nodeVo : nodeList) {
                    if (nodeVo.getOutId() != null && nodeVo.getOutIdType() != null) {
                        Integer nodeOutIdType = nodeVo.getOutIdType();
                        Long nodeOutId = nodeVo.getOutId();

                        // 如果map中还没有这个outIdType，创建新的列表
                        resultMap.computeIfAbsent(nodeOutIdType, k -> new ArrayList<>()).add(nodeOutId);
                    }
                }

                totalNodes += nodeList.size();
                log.debug("Found {} nodes on page {} for account: {}", nodeList.size(), page, account);

                // 检查是否是最后一页：返回的数据量小于页面大小
                if (nodeList.size() < TPC_NODE_PAGE_SIZE) {
                    log.debug("Reached last page {} for account: {} (returned {} < {})",
                            page, account, nodeList.size(), TPC_NODE_PAGE_SIZE);
                    break;
                }

                // 检查是否有总数信息，如果有则可以更精确地判断是否结束
                if (result.getData().getTotal() > 0) {
                    long expectedTotal = result.getData().getTotal();
                    if (totalNodes >= expectedTotal) {
                        log.debug("Reached expected total {} nodes for account: {}", expectedTotal, account);
                        break;
                    }
                }

                page++;
            }

            // 检查是否因为达到最大页数而退出
            if (page > maxPageSize) {
                log.warn("Reached maximum pages limit ({}) for account: {}, this might indicate an issue",
                        maxPageSize, account);
            }

            log.debug("Found total {} nodes for account: {}, grouped by outIdType: {}",
                    totalNodes, account, resultMap.keySet());

            if (log.isTraceEnabled()) {
                resultMap.forEach((outIdType, nodeIds) -> log.trace("OutIdType {}: {} nodes with IDs: {}", outIdType,
                        nodeIds.size(), nodeIds));
            }

            return resultMap;
        } catch (Exception e) {
            log.error("Error getting node IDs for account: {}", account, e);
            return resultMap;
        }
    }

    @Override
    public boolean checkUserNodePermission(String userAccount, Long nodeId) {
        if (userAccount == null || userAccount.trim().isEmpty() || nodeId == null) {
            log.warn("Invalid parameters for checkUserNodePermission: userAccount={}, nodeId={}",
                    userAccount, nodeId);
            return false;
        }

        try {
            // 检查用户对节点的权限 - 简化实现，暂时返回true
            // 实际实现需要调用TPC API
            log.debug("checkUserNodePermission called for user: {}, nodeId: {}, returning true", userAccount, nodeId);
            return true;
        } catch (Exception e) {
            log.error("Error checking user permission: userAccount={}, nodeId={}", userAccount, nodeId, e);
            return false;
        }
    }

    @Override
    public boolean addUserToScene(Long sceneId, String memberAccount, String ownerAccount) {
        return addUserToNode(sceneId, memberAccount, ownerAccount, SCENE_TYPE_CODE, "scene");
    }

    @Override
    public boolean addUserToMetric(Long metricId, String memberAccount, String ownerAccount) {
        return addUserToNode(metricId, memberAccount, ownerAccount, METRIC_TYPE_CODE, "metric");
    }

    @Override
    public boolean removeUserFromScene(Long sceneId, String memberAccount, String ownerAccount) {
        log.info("Removing user from scene: sceneId={}, memberAccount={}, ownerAccount={}",
                sceneId, memberAccount, ownerAccount);
        return removeUserFromNode(sceneId, memberAccount, ownerAccount, SCENE_TYPE_CODE, "scene");
    }

    @Override
    public boolean removeUserFromMetric(Long metricId, String memberAccount, String ownerAccount) {
        log.info("Removing user from metric: metricId={}, memberAccount={}, ownerAccount={}",
                metricId, memberAccount, ownerAccount);
        return removeUserFromNode(metricId, memberAccount, ownerAccount, METRIC_TYPE_CODE, "metric");
    }

    @Override
    public PageResult<UserPermissionInfoResp> listUserBySceneId(Long sceneId, String ownerAccount, int page,
                                                                int pageSize) {
        return listUserByNodeId(sceneId, ownerAccount, page, pageSize, SCENE_TYPE_CODE);
    }

    @Override
    public PageResult<UserPermissionInfoResp> listUserByMetricId(Long metricId, String ownerAccount, int page,
            int pageSize) {
        return listUserByNodeId(metricId, ownerAccount, page, pageSize, METRIC_TYPE_CODE);
    }

    /**
     * 查询节点的用户权限列表
     */
    private PageResult<UserPermissionInfoResp> listUserByNodeId(Long nodeId, String ownerAccount, int page,
            int pageSize, Integer outIdType) {
        if (nodeId == null || ownerAccount == null || ownerAccount.trim().isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0L, page, pageSize);
        }

        try {
            NodeUserQryParam param = new NodeUserQryParam();
            param.setAccount(ownerAccount);
            param.setUserType(USER_ACCOUNT_TYPE);
            param.setOutId(nodeId);
            param.setOutIdType(outIdType);
            param.setPage(page);
            param.setPageSize(pageSize);
            param.setPager(Boolean.TRUE);

            Result<PageDataVo<NodeUserRelVo>> result = nodeUserFacade.list(param);

            if (!isSuccess(result) || result.getData() == null) {
                return new PageResult<>(new ArrayList<>(), 0L, page, pageSize);
            }

            PageDataVo<NodeUserRelVo> pageData = result.getData();
            List<UserPermissionInfoResp> userPermissionList = new ArrayList<>();

            if (pageData.getList() != null) {
                for (NodeUserRelVo nodeUserRel : pageData.getList()) {
                    userPermissionList.add(convertToUserPermissionInfo(nodeUserRel));
                }
            }

            return new PageResult<>(userPermissionList, (long) pageData.getTotal(), page, pageSize);

        } catch (Exception e) {
            log.error("Error listing users for nodeId: {}, account: {}", nodeId, ownerAccount, e);
            return new PageResult<>(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    /**
     * 将NodeUserRelVo转换为UserPermissionInfoResp
     *
     * @param nodeUserRel TPC用户节点关系对象
     * @return 用户权限信息对象
     */
    private UserPermissionInfoResp convertToUserPermissionInfo(NodeUserRelVo nodeUserRel) {
        UserPermissionInfoResp userPermission = new UserPermissionInfoResp();
        userPermission.setAccount(nodeUserRel.getAccount());

        Integer roleCode = nodeUserRel.getType();
        String roleName;

        if (roleCode == null) {
            roleCode = UserRole.MEMBER.getCode();
            roleName = UserRole.MEMBER.getDesc();
        } else {
            switch (roleCode) {
                case 0:
                    roleName = UserRole.ADMIN.getDesc();
                    break;
                case 1:
                    roleName = UserRole.MEMBER.getDesc();
                    break;
                default:
                    log.warn("Unknown role code: {}, defaulting to MEMBER", roleCode);
                    roleCode = UserRole.MEMBER.getCode();
                    roleName = UserRole.MEMBER.getDesc();
                    break;
            }
        }

        userPermission.setRoleCode(roleCode);
        userPermission.setRoleName(roleName);

        return userPermission;
    }

    /**
     * 通用方法：添加用户到节点
     *
     * @param nodeId        节点ID
     * @param memberAccount 成员账号
     * @param ownerAccount  拥有者账号
     * @param outIdType     节点类型代码
     * @param nodeTypeName  节点类型名称（用于日志）
     * @return 是否添加成功
     */
    private boolean addUserToNode(Long nodeId, String memberAccount, String ownerAccount,
            Integer outIdType, String nodeTypeName) {
        // 参数验证
        if (nodeId == null || memberAccount == null || ownerAccount == null) {
            log.warn("Invalid parameters for addUserTo{}: {}Id={}, memberAccount={}, ownerAccount={}",
                    nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount);
            return false;
        }

        // 检查是否为自我授权
        if (memberAccount.equals(ownerAccount)) {
            log.warn("Self-authorization attempt blocked: account={}, {}Id={}, nodeType={}",
                    ownerAccount, nodeTypeName, nodeId, nodeTypeName);
            return false;
        }

        try {
            // 构建请求参数
            NodeUserAddParam param = new NodeUserAddParam();
            param.setAccount(ownerAccount);
            param.setUserType(USER_ACCOUNT_TYPE);
            param.setMemberAcc(memberAccount);
            param.setMemberAccType(USER_ACCOUNT_TYPE);
            param.setOutId(nodeId);
            param.setOutIdType(outIdType);
            param.setType(NodeUserRelTypeEnum.MEMBER.getCode());

            // 调用TPC API
            Result<NodeUserRelVo> result = nodeUserFacade.add(param);

            // 记录操作结果
            if (isSuccess(result)) {
                log.debug("Successfully added user to {}: {}Id={}, memberAccount={}, ownerAccount={}",
                        nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount);
                return true;
            } else {
                log.warn("Failed to add user to {}: {}Id={}, memberAccount={}, ownerAccount={}, result={}",
                        nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount, result.getMessage());
                return false;
            }

        } catch (Exception e) {
            log.error("Error adding user to {}: {}Id={}, memberAccount={}, ownerAccount={}",
                    nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount, e);
            return false;
        }
    }

    /**
     * 通用方法：撤销用户节点权限
     *
     * @param nodeId        节点ID
     * @param memberAccount 成员账号
     * @param ownerAccount  拥有者账号
     * @param outIdType     节点类型代码
     * @param nodeTypeName  节点类型名称（用于日志）
     * @return 是否撤销成功
     */
    private boolean removeUserFromNode(Long nodeId, String memberAccount, String ownerAccount,
            Integer outIdType, String nodeTypeName) {
        // 参数验证
        if (nodeId == null || memberAccount == null || ownerAccount == null) {
            log.warn("Invalid parameters for removeUserFrom{}: {}Id={}, memberAccount={}, ownerAccount={}",
                    nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount);
            return false;
        }

        // 检查是否为自我撤权
        if (memberAccount.equals(ownerAccount)) {
            log.warn("Self-revocation attempt blocked: account={}, {}Id={}, nodeType={}",
                    ownerAccount, nodeTypeName, nodeId, nodeTypeName);
            return false;
        }

        try {
            // 构建删除请求参数
            NodeUserDeleteParam param = new NodeUserDeleteParam();
            param.setAccount(ownerAccount);
            param.setUserType(USER_ACCOUNT_TYPE);
            param.setDelAcc(memberAccount);
            param.setDelUserType(USER_ACCOUNT_TYPE);
            param.setOutId(nodeId);
            param.setOutIdType(outIdType);

            // 调用TPC API删除权限
            Result result = nodeUserFacade.delete(param);

            // 记录操作结果
            if (isSuccess(result)) {
                log.debug("Successfully removed user from {}: {}Id={}, memberAccount={}, ownerAccount={}",
                        nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount);
                return true;
            } else {
                log.warn("Failed to remove user from {}: {}Id={}, memberAccount={}, ownerAccount={}, result={}",
                        nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount, result.getMessage());
                return false;
            }

        } catch (Exception e) {
            log.error("Error removing user from {}: {}Id={}, memberAccount={}, ownerAccount={}",
                    nodeTypeName, nodeTypeName, nodeId, memberAccount, ownerAccount, e);
            return false;
        }
    }

    /**
     * 获取用户的内部组织ID
     *
     * @param account 用户账号
     * @return 组织ID，异常情况下返回-1
     */
    private Long getInnerOrgId(String account) {
        // 1. 参数验证
        if (account == null || account.trim().isEmpty()) {
            log.warn("Account is null or empty for getInnerOrgId");
            return -1L;
        }

        try {
            // 2. 获取用户信息
            Map<String, String> userInfoMap = getUserInfo(account);
            if (userInfoMap == null || userInfoMap.isEmpty()) {
                log.warn("No user info found for account: {}", account);
                return -1L;
            }

            // 3. 获取idPath
            String idPath = userInfoMap.get("idPath");
            if (idPath == null || idPath.trim().isEmpty()) {
                log.warn("IdPath is null or empty for account: {}, userInfo: {}", account, userInfoMap);
                return -1L;
            }

            // 4. 查询组织映射
            TpcOutIdOrgMappingDO tpcOutIdObj = null;
            try {
                tpcOutIdObj = tpcOutIdOrgMappingDao.getByIdPath(idPath);
            } catch (Exception e) {
                log.error("Failed to query org mapping by idPath: {} for account: {}", idPath, account, e);
                return -1L;
            }

            // 5. 验证查询结果
            if (tpcOutIdObj == null) {
                log.warn("No org mapping found for idPath: {} and account: {}", idPath, account);
                return -1L;
            }

            // 6. 验证ID字段
            Long orgId = tpcOutIdObj.getId();
            if (orgId == null) {
                log.warn("Org mapping ID is null for idPath: {} and account: {}", idPath, account);
                return -1L;
            }

            log.debug("Successfully got inner org ID: {} for account: {} with idPath: {}", orgId, account, idPath);
            return orgId;

        } catch (Exception e) {
            log.error("Unexpected error getting inner org ID for account: {}", account, e);
            return -1L;
        }
    }

    private Map<String, String> getUserInfo(String fullAccount) {
        Map<String, String> userInfos = Maps.newHashMap();
        NullParam param = new NullParam();
        param.setAccount(fullAccount);
        param.setUserType(UserTypeEnum.CAS_TYPE.getCode());
        Result<OrgInfoVo> orgByAccount = userOrgFacade.getOrgByAccount(param);
        if (orgByAccount == null || orgByAccount.getData() == null) {
            log.debug("No organization data found for account: {}", fullAccount);
        } else {
            userInfos.put("idPath", orgByAccount.getData().getIdPath());
            userInfos.put("namePath", orgByAccount.getData().getNamePath());
            log.debug("Retrieved user org info for account: {}, idPath: {}, namePath: {}",
                    fullAccount, orgByAccount.getData().getIdPath(), orgByAccount.getData().getNamePath());
        }

        return userInfos;
    }

    private boolean isSuccess(Result result) {
        if (null == result) {
            log.warn("Result is null in isSuccess check");
            return false;
        }

        return GeneralCodes.OK.getCode() == result.getCode();
    }

    /**
     * 从组织架构路径中提取最后一个部分作为节点名称
     *
     * @param path 组织架构路径，如："小米公司/集团信息技术部/销服研发部/研发效能"
     * @return 最后一个部分的名称，如："研发效能"，如果无效则抛出异常
     * @throws IllegalArgumentException 当路径为空或提取的节点名称为空时
     */
    private String extractLastPartOfPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            log.error("Organization path is null or empty: {}", path);
            throw new IllegalArgumentException("Organization path cannot be null or empty");
        }

        // 去除前后空白并统一分隔符
        String normalizedPath = path.trim().replace("\\", "/");

        // 去除末尾的斜杠（如果有）
        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        // 分割路径
        String[] parts = normalizedPath.split("/");

        if (parts.length == 0) {
            log.error("No valid parts found in path: {}", path);
            throw new IllegalArgumentException("No valid parts found in organization path: " + path);
        }

        // 获取最后一个部分
        String lastPart = parts[parts.length - 1];

        if (lastPart == null || lastPart.trim().isEmpty()) {
            log.error("Last part of path is empty: {}", path);
            throw new IllegalArgumentException("Last part of organization path is empty: " + path);
        }

        String nodeName = lastPart.trim();
        log.debug("Extracted node name '{}' from path '{}'", nodeName, path);

        return nodeName;
    }

    /**
     * 获取用户的内部组织ID（公共方法）
     *
     * @param account 用户账号
     * @return 内部组织ID，如果获取失败返回-1L
     */
    @Override
    public Long getInnerOrgIdForUser(String account) {
        try {
            return getInnerOrgId(account);
        } catch (Exception e) {
            log.error("Failed to get inner org id for account: {}", account, e);
            return -1L;
        }
    }

    @Override
    public List<String> getSubscribeUserAccounts() {
        Set<String> accountSet = new HashSet<>();

        try {
            log.info("开始获取订阅用户账号列表，组ID: {}, 超级管理员: {}", MGR_GROUP_ID, SUPER_MGR_ACCOUNT);

            UserGroupMemberQryParam param = new UserGroupMemberQryParam();
            param.setAccount(SUPER_MGR_ACCOUNT);
            param.setUserType(USER_ACCOUNT_TYPE);
            param.setGroupId(MGR_GROUP_ID);
            param.setPage(1);
            param.setPageSize(100);
            param.setPager(true);

            int totalCount = 0;
            int page = 1;
            int maxPages = 10; // 最大页数限制，防止死循环

            while (page <= maxPages) {
                param.setPage(page);

                Result<PageDataVo<UserGroupRelVo>> result = userGroupFacade.listGroupMembers(param);

                if (!isSuccess(result)) {
                    log.error("获取用户组成员失败，页码: {}, 错误信息: {}", page,
                            result != null ? result.getMessage() : "未知错误");
                    break;
                }

                PageDataVo<UserGroupRelVo> pageData = result.getData();
                if (pageData == null || pageData.getList() == null || pageData.getList().isEmpty()) {
                    log.debug("第{}页没有数据，结束查询", page);
                    break;
                }

                List<UserGroupRelVo> memberList = pageData.getList();
                for (UserGroupRelVo userGroupRelVo : memberList) {
                    String account = userGroupRelVo.getAccount();
                    if (account != null && !account.trim().isEmpty()) {
                        accountSet.add(account.trim());
                    }
                }

                totalCount += memberList.size();
                log.debug("第{}页获取到{}个用户账号", page, memberList.size());

                // 如果当前页数据量小于页面大小，说明是最后一页
                if (memberList.size() < param.getPageSize()) {
                    log.debug("已到达最后一页，页码: {}", page);
                    break;
                }

                // 如果有总数信息，检查是否已获取完所有数据
                if (pageData.getTotal() > 0 && totalCount >= pageData.getTotal()) {
                    log.debug("已获取所有数据，总数: {}", pageData.getTotal());
                    break;
                }

                page++;
            }

            if (page > maxPages) {
                log.warn("达到最大页数限制 {}, 可能存在更多数据未获取", maxPages);
            }

            List<String> resultList = new ArrayList<>(accountSet);
            log.info("成功获取订阅用户账号列表，共{}个用户: {}", resultList.size(), resultList);

            return resultList;

        } catch (Exception e) {
            log.error("获取订阅用户账号列表时发生异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void sentEventMsg(List<String> accounts, String message) {
        // 先简单输出日志，后续确定好消息接口后在改造，暂时不用改，这个方法也不用优化
        accounts.stream().forEach(account -> {
            log.info("sent msg: {} \nto account: {}", message, account);
        });
    }

    @Override
    public ResourcePermission calculateScenePermission(
            Long sceneId, String sceneOwnerId, String currentAccount) {

        if (sceneId == null || currentAccount == null || currentAccount.trim().isEmpty()) {
            return ResourcePermission.viewOnlyPermission();
        }

        try {
            // 1. 检查是否为创建者
            if (sceneOwnerId != null && sceneOwnerId.equals(currentAccount)) {
                log.debug("User {} is owner of scene {}", currentAccount, sceneId);
                return ResourcePermission.fullPermission();
            }

            // 2. 检查是否为超级管理员
            if (SUPER_MGR_ACCOUNT.equals(currentAccount)) {
                log.debug("User {} is super admin", currentAccount);
                return ResourcePermission.fullPermission();
            }

            // 3. 检查是否有明确的场景权限
            List<Long> userSceneIds = getSceneNodeIds(currentAccount);
            if (userSceneIds != null && userSceneIds.contains(sceneId)) {
                log.debug("User {} has explicit permission for scene {}", currentAccount, sceneId);
                return ResourcePermission.memberPermission();
            }

            // 4. 默认返回只读权限（如果需要，后续可以考虑组织权限等）
            log.debug("User {} has no explicit permission for scene {}, returning view-only", currentAccount, sceneId);
            return ResourcePermission.viewOnlyPermission();

        } catch (Exception e) {
            log.error("Error calculating scene permission for user: {}, sceneId: {}", currentAccount, sceneId, e);
            return ResourcePermission.viewOnlyPermission();
        }
    }

    @Override
    public ResourcePermission calculateMetricPermission(
            Long metricId, String metricOwnerId, String currentAccount) {

        if (metricId == null || currentAccount == null || currentAccount.trim().isEmpty()) {
            return ResourcePermission.viewOnlyPermission();
        }

        try {
            // 1. 检查是否为创建者
            if (metricOwnerId != null && metricOwnerId.equals(currentAccount)) {
                log.debug("User {} is owner of metric {}", currentAccount, metricId);
                return ResourcePermission.fullPermission();
            }

            // 2. 检查是否为超级管理员
            if (SUPER_MGR_ACCOUNT.equals(currentAccount)) {
                log.debug("User {} is super admin", currentAccount);
                return ResourcePermission.fullPermission();
            }

            // 3. 检查是否有明确的指标权限
            List<Long> userMetricIds = getMetricNodeIds(currentAccount);
            if (userMetricIds != null && userMetricIds.contains(metricId)) {
                log.debug("User {} has explicit permission for metric {}", currentAccount, metricId);
                return ResourcePermission.memberPermission();
            }

            // 4. 默认返回只读权限
            log.debug("User {} has no explicit permission for metric {}, returning view-only", currentAccount,
                    metricId);
            return ResourcePermission.viewOnlyPermission();

        } catch (Exception e) {
            log.error("Error calculating metric permission for user: {}, metricId: {}", currentAccount, metricId, e);
            return ResourcePermission.viewOnlyPermission();
        }
    }

    @Override
    public Map<Long, ResourcePermission> batchCalculateScenePermissions(
            Map<Long, String> sceneInfos, String currentAccount) {

        Map<Long, ResourcePermission> result = new HashMap<>();

        if (sceneInfos == null || sceneInfos.isEmpty() || currentAccount == null || currentAccount.trim().isEmpty()) {
            return result;
        }

        try {
            // 批量获取用户权限列表（优化性能）
            List<Long> userSceneIds = getSceneNodeIds(currentAccount);
            boolean isSuperAdmin = SUPER_MGR_ACCOUNT.equals(currentAccount);

            for (Map.Entry<Long, String> entry : sceneInfos.entrySet()) {
                Long sceneId = entry.getKey();
                String ownerId = entry.getValue();

                ResourcePermission permission;

                if (isSuperAdmin || (ownerId != null && ownerId.equals(currentAccount))) {
                    permission = ResourcePermission.fullPermission();
                } else if (userSceneIds != null && userSceneIds.contains(sceneId)) {
                    permission = ResourcePermission.memberPermission();
                } else {
                    permission = ResourcePermission.viewOnlyPermission();
                }

                result.put(sceneId, permission);
            }

            log.debug("Batch calculated permissions for {} scenes for user {}", sceneInfos.size(), currentAccount);
            return result;

        } catch (Exception e) {
            log.error("Error batch calculating scene permissions for user: {}", currentAccount, e);
            // 发生错误时，给所有场景默认只读权限
            for (Long sceneId : sceneInfos.keySet()) {
                result.put(sceneId, ResourcePermission.viewOnlyPermission());
            }
            return result;
        }
    }

    @Override
    public Map<Long, ResourcePermission> batchCalculateMetricPermissions(
            Map<Long, String> metricInfos, String currentAccount) {

        Map<Long, ResourcePermission> result = new HashMap<>();

        if (metricInfos == null || metricInfos.isEmpty() || currentAccount == null || currentAccount.trim().isEmpty()) {
            return result;
        }

        try {
            // 批量获取用户权限列表（优化性能）
            List<Long> userMetricIds = getMetricNodeIds(currentAccount);
            boolean isSuperAdmin = SUPER_MGR_ACCOUNT.equals(currentAccount);

            for (Map.Entry<Long, String> entry : metricInfos.entrySet()) {
                Long metricId = entry.getKey();
                String ownerId = entry.getValue();

                ResourcePermission permission;

                if (isSuperAdmin || (ownerId != null && ownerId.equals(currentAccount))) {
                    permission = ResourcePermission.fullPermission();
                } else if (userMetricIds != null && userMetricIds.contains(metricId)) {
                    permission = ResourcePermission.memberPermission();
                } else {
                    permission = ResourcePermission.viewOnlyPermission();
                }

                result.put(metricId, permission);
            }

            log.debug("Batch calculated permissions for {} metrics for user {}", metricInfos.size(), currentAccount);
            return result;

        } catch (Exception e) {
            log.error("Error batch calculating metric permissions for user: {}", currentAccount, e);
            // 发生错误时，给所有指标默认只读权限
            for (Long metricId : metricInfos.keySet()) {
                result.put(metricId, ResourcePermission.viewOnlyPermission());
            }
            return result;
        }
    }
}
