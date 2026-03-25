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
package org.apache.ozhera.log.manager.service.impl;


import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.api.model.agent.AgentApiResult;
import org.apache.ozhera.log.api.model.agent.SpaceInfo;
import org.apache.ozhera.log.api.model.agent.StoreInfo;
import org.apache.ozhera.log.api.model.agent.TailInfo;
import org.apache.ozhera.log.api.model.agent.UserInfo;
import org.apache.ozhera.log.api.service.HeraLogApiService;
import org.apache.ozhera.log.api.service.LogAgentApiService;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.domain.Tpc;
import org.apache.ozhera.log.manager.model.MilogSpaceParam;
import org.apache.ozhera.log.manager.model.bo.LogTailParam;
import org.apache.ozhera.log.manager.model.dto.LogStoreDTO;
import org.apache.ozhera.log.manager.model.dto.LogTailDTO;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceDTO;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceTreeDTO;
import org.apache.ozhera.log.manager.model.dto.MotorRoomDTO;
import org.apache.ozhera.log.manager.model.vo.LogStoreParam;
import org.apache.ozhera.log.manager.service.HeralogHomePageService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(interfaceClass = LogAgentApiService.class, group = "$dubbo.group", timeout = 10000)
public class LogAgentApiServiceImpl implements LogAgentApiService {

    @Resource
    private LogSpaceServiceImpl logSpaceService;

    @Resource
    private LogStoreServiceImpl logStoreService;

    @Resource
    private LogTailServiceImpl logTailService;

    @Resource
    private HeralogHomePageService heralogHomePageService;

    @Resource
    private Tpc tpc;

    @Override
    public String createSpace(SpaceInfo info) {
        if (info.getSpaceName() == null || info.getSpaceName().isEmpty()) {
            return AgentApiResult.fail("The space name cannot be empty!");
        }
        if (info.getSpaceDescription() == null || info.getSpaceDescription().isEmpty()) {
            return AgentApiResult.fail("The space description cannot be empty!");
        }
        if (info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty()) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }

        MilogSpaceParam param = new MilogSpaceParam();
        param.setSpaceName(info.getSpaceName());
        param.setDescription(info.getSpaceDescription());
        try {
            setMoneUserContext(info.getUserInfo());
            Result<String> result = logSpaceService.newMilogSpace(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Create space failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("spaceId", result.getData())
                    .build();
        } catch (Exception e) {
            log.error("create space failed, name: {}, description: {}, error msg: {}", info.getSpaceName(), info.getSpaceDescription(), e.getMessage());
            return AgentApiResult.fail("Create space failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String updateSpace(SpaceInfo info) {
        if (info.getSpaceId() == null || info.getSpaceId() < 0) {
            return AgentApiResult.fail("The space id cannot be empty!");
        }
        if (info.getSpaceName() == null || info.getSpaceName().isEmpty()) {
            return AgentApiResult.fail("The space name cannot be empty!");
        }
        if (info.getSpaceDescription() == null || info.getSpaceDescription().isEmpty()) {
            return AgentApiResult.fail("The space description cannot be empty!");
        }
        if (info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty()) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }

        MilogSpaceParam param = new MilogSpaceParam();
        param.setId(info.getSpaceId());
        param.setSpaceName(info.getSpaceName());
        param.setDescription(info.getSpaceDescription());

        try {
            setMoneUserContext(info.getUserInfo());
            Result<String> result = logSpaceService.updateMilogSpace(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Update space failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Update space failed, name: {}, description: {}, error msg: {}", info.getSpaceName(), info.getSpaceDescription(), e.getMessage());
            return AgentApiResult.fail("Update space failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String deleteSpace(SpaceInfo info) {
        if (info.getSpaceId() == null || info.getSpaceId() < 0) {
            return AgentApiResult.fail("The space id cannot be empty!");
        }
        if (info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty() || info.getUserInfo().getUserType() == null) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }

        try {
            setMoneUserContext(info.getUserInfo());
            Result<String> result = logSpaceService.deleteMilogSpace(info.getSpaceId());
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Delete space failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Delete space failed, space id: {}, error msg: {}", info.getSpaceId(), e.getMessage());
            return AgentApiResult.fail("Delete space failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String getSpaceById(Long spaceId) {
        try {
            Result<MilogSpaceDTO> result = logSpaceService.getMilogSpaceById(spaceId);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Get space failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("space", result.getData())
                    .build();
        } catch (Exception e) {
            log.error("Get space failed! space id: {}, message: {}", spaceId, e.getMessage());
            return AgentApiResult.fail("Get space failed, the reason for the failure is " + e.getMessage());
        }
    }

    @Override
    public String getStoresInSpace(Long spaceId) {
        try {
            Result<List<MilogSpaceTreeDTO>> tree = heralogHomePageService.getMilogSpaceTree(spaceId);
            if (tree.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Failed to obtain the list of stores in the current space, the reason is " + tree.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("stores", tree.getData())
                    .build();
        } catch (Exception e) {
            log.error("Failed to obtain the list of stores in the current space, spaceId: {}, the reason is {}", spaceId, e.getMessage());
            return AgentApiResult.fail("Failed to obtain the list of stores in the current space, the reason is " + e.getMessage());
        }
    }

    @Override
    public String getStoreInfoById(StoreInfo info) {
        if (info.getStoreId() == null || info.getStoreId() < 0) {
            return AgentApiResult.fail("The store id cannot be empty!");
        }
        if (info.getUserInfo() == null || info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty()) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }
        try {
            setMoneUserContext(info.getUserInfo());
            Result<LogStoreDTO> result = logStoreService.getLogStoreById(info.getStoreId());
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Get store failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("store", result.getData())
                    .build();
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String createStore(StoreInfo info) {
        String validationResult = info.isValidParam(true);
        if (validationResult != null) {
            return AgentApiResult.fail(validationResult);
        }

        LogStoreParam param = buildLogStoreParam(info, true);
        try {
            setMoneUserContext(info.getUserInfo());
            Result<String> result = logStoreService.newLogStore(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Create store failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Create store failed, name: {}, spaceId: {}, error msg: {}", info.getStoreName(), info.getSpaceId(), e.getMessage());
            return AgentApiResult.fail("Create store failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String updateStore(StoreInfo info) {
        String validationResult = info.isValidParam(false);
        if (validationResult != null) {
            return AgentApiResult.fail(validationResult);
        }

        LogStoreParam param = buildLogStoreParam(info, false);
        try {
            setMoneUserContext(info.getUserInfo());
            Result<String> result = logStoreService.newLogStore(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Update store failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Update store failed, name: {}, storeId: {}, error msg: {}", info.getStoreName(), info.getStoreId(), e.getMessage());
            return AgentApiResult.fail("Update store failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String deleteStore(StoreInfo info) {
        if (info.getStoreId() == null || info.getStoreId() < 0) {
            return AgentApiResult.fail("The store id cannot be empty!");
        }
        if (info.getUserInfo() == null || info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty()) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }

        try {
            setMoneUserContext(info.getUserInfo());
            Result<Void> result = logStoreService.deleteLogStore(info.getStoreId());
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Delete store failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Delete store failed, storeId: {}, error msg: {}", info.getStoreId(), e.getMessage());
            return AgentApiResult.fail("Delete store failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    private LogStoreParam buildLogStoreParam(StoreInfo info, boolean isCreate) {
        LogStoreParam param = new LogStoreParam();
        if (!isCreate) {
            param.setId(info.getStoreId());
        }
        param.setSpaceId(info.getSpaceId());
        param.setLogstoreName(info.getStoreName());
        param.setStorePeriod(info.getStorePeriod());
        param.setShardCnt(info.getShardCnt());
        param.setKeyList(info.getKeyList());
        param.setColumnTypeList(info.getColumnTypeList());
        if (info.getLogType() != null) {
            param.setLogType(Integer.parseInt(info.getLogType()));
        }
        if (info.getMachineRoom() != null) {
            param.setMachineRoom(String.valueOf(info.getMachineRoom()));
        }
        if (info.getMqResourceId() != null) {
            param.setMqResourceId(Long.valueOf(info.getMqResourceId()));
        }
        if (info.getEsResourceId() != null) {
            param.setEsResourceId(Long.valueOf(info.getEsResourceId()));
        }
        return param;
    }

    @Override
    public String createTail(TailInfo info) {
        String validationResult = info.isValidParam(true);
        if (validationResult != null) {
            return AgentApiResult.fail(validationResult);
        }

        LogTailParam param = buildLogTailParam(info, true);
        try {
            setMoneUserContext(info.getUserInfo());
            Result<LogTailDTO> result = logTailService.newMilogLogTail(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Create tail failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("tailId", result.getData().getId())
                    .build();
        } catch (Exception e) {
            log.error("Create tail failed, tail: {}, storeId: {}, error msg: {}", info.getTail(), info.getStoreId(), e.getMessage());
            return AgentApiResult.fail("Create tail failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String updateTail(TailInfo info) {
        String validationResult = info.isValidParam(false);
        if (validationResult != null) {
            return AgentApiResult.fail(validationResult);
        }

        LogTailParam param = buildLogTailParam(info, false);
        try {
            setMoneUserContext(info.getUserInfo());
            Result<Void> result = logTailService.updateMilogLogTail(param);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Update tail failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Update tail failed, tail: {}, tailId: {}, error msg: {}", info.getTail(), info.getId(), e.getMessage());
            return AgentApiResult.fail("Update tail failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String deleteTail(TailInfo info) {
        if (info.getId() == null || info.getId() < 0) {
            return AgentApiResult.fail("The tail id cannot be empty!");
        }
        if (info.getUserInfo() == null || info.getUserInfo().getUser() == null || info.getUserInfo().getUser().isEmpty()) {
            return AgentApiResult.fail("The user information is empty. Please provide the correct user information!");
        }

        try {
            setMoneUserContext(info.getUserInfo());
            Result<Void> result = logTailService.deleteLogTail(info.getId());
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Delete tail failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.success();
        } catch (Exception e) {
            log.error("Delete tail failed, tailId: {}, error msg: {}", info.getId(), e.getMessage());
            return AgentApiResult.fail("Delete tail failed, the reason for the failure is " + e.getMessage());
        } finally {
            MoneUserContext.clear();
        }
    }

    @Override
    public String getTailById(Long tailId) {
        if (tailId == null || tailId < 0) {
            return AgentApiResult.fail("The tail id cannot be empty!");
        }

        try {
            Result<LogTailDTO> result = logTailService.getMilogLogtailById(tailId);
            if (result.getCode() != CommonError.Success.getCode()) {
                return AgentApiResult.fail("Get tail failed, the reason for the failure is " + result.getMessage());
            }
            return AgentApiResult.successBuilder()
                    .addData("tail", result.getData())
                    .build();
        } catch (Exception e) {
            log.error("Get tail failed, tailId: {}, error msg: {}", tailId, e.getMessage());
            return AgentApiResult.fail("Get tail failed, the reason for the failure is " + e.getMessage());
        }
    }


    private void setMoneUserContext(UserInfo userInfo) {
        AuthUserVo authUserVo = new AuthUserVo();
        authUserVo.setAccount(userInfo.getUser());
        authUserVo.setUserType(userInfo.getUserType());
        boolean isAdmin = tpc.isAdmin(userInfo.getUser(), userInfo.getUserType());
        MoneUserContext.setCurrentUser(authUserVo, isAdmin);
    }

    private LogTailParam buildLogTailParam(TailInfo info, boolean isCreate) {
        LogTailParam param = new LogTailParam();
        if (!isCreate) {
            param.setId(info.getId());
        }
        param.setSpaceId(info.getSpaceId());
        param.setStoreId(info.getStoreId());
        param.setMilogAppId(info.getMilogAppId());
        param.setAppId(info.getAppId());
        param.setAppName(info.getAppName());
        param.setEnvId(info.getEnvId());
        param.setEnvName(info.getEnvName());
        param.setIps(info.getIps());
        param.setTail(info.getTail());
        param.setParseType(info.getParseType());
        param.setParseScript(info.getParseScript());
        param.setLogPath(info.getLogPath());
        param.setValueList(info.getValueList());
        param.setTailRate(info.getTailRate());
        param.setLogSplitExpress(info.getLogSplitExpress());
        param.setFirstLineReg(info.getFirstLineReg());
        param.setAppType(info.getAppType());
        param.setMachineType(info.getMachineType());
        param.setDeployWay(info.getDeployWay());
        param.setBatchSendSize(info.getBatchSendSize());
        param.setCollectionReady(info.getCollectionReady());

        if (info.getMotorRooms() != null) {
            List<MotorRoomDTO> motorRoomDTOList = new ArrayList<>();
            for (Object item : info.getMotorRooms()) {
                if (item instanceof MotorRoomDTO) {
                    motorRoomDTOList.add((MotorRoomDTO) item);
                }
            }
            param.setMotorRooms(motorRoomDTOList);
        }

        if (info.getMiddlewareConfig() != null) {
            param.setMiddlewareConfig(info.getMiddlewareConfig());
        }

        return param;
    }
}
