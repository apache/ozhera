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
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.app.model.vo.HeraEnvIpVo;
import org.apache.ozhera.log.api.enums.LogStructureEnum;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.bo.LogTailParam;
import org.apache.ozhera.log.manager.model.bo.MlogParseParam;
import org.apache.ozhera.log.manager.model.dto.*;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.model.vo.QuickQueryVO;

import java.util.List;
import java.util.Map;

public interface LogTailService {

    Result<LogTailDTO> newMilogLogTail(LogTailParam param);

    void sengMessageNewTail(LogTailParam param, MilogLogTailDo milogLogtailDo, MilogLogStoreDO milogLogStore);

    MilogLogTailDo buildLogTailDo(LogTailParam param, MilogLogStoreDO milogLogStore, AppBaseInfo appBaseInfo, String creator);

    void sengMessageToAgent(Long milogAppId, MilogLogTailDo logtailDo);

    void sengMessageToStream(MilogLogTailDo mt, Integer type);

    void handleNacosConfigByMotorRoom(MilogLogTailDo mt, String motorRoomEn, Integer type, Integer projectType);

    /**
     * Delete a part of a configuration in the configuration center
     *
     * @param spaceId
     * @param id
     * @param motorRoomEn
     * @param logStructureEnum
     * @return
     */
    boolean deleteConfigRemote(Long spaceId, Long id, String motorRoomEn, LogStructureEnum logStructureEnum);

    Result<LogTailDTO> getMilogLogtailById(Long id);

    Result<Map<String, Object>> getMilogLogBypage(Long storeId, String tailName, int page, int pagesize);

    Result<Map<String, Object>> getLogTailCountByStoreId(Long storeId);

    Result<List<LogTailDTO>> getMilogLogtailByIds(List<Long> ids);

    Result<Void> updateMilogLogTail(LogTailParam param);

    Result<Void> deleteLogTail(Long id);

    void sendMessageOnDelete(MilogLogTailDo mt, MilogLogStoreDO logStoreDO);

    Result<List<MapDTO>> getAppInfoByName(String appName, Integer type);

    /**
     * If the application is milog-agent, get a list of all machines through an additional interface
     *
     * @param milogAppId
     * @param deployWay
     * @return
     */
    Result<List<MilogAppEnvDTO>> getEnInfosByAppId(Long milogAppId, Integer deployWay, String machineRoom);

    Result<List<String>> getTailNamesBystoreId(String tail, Integer appType, Long id);

    Result<List<MapDTO<String, String>>> tailRatelimit();

    /***
     * miline Dynamic expansion and contraction
     * @param projectInfo
     */
    void dockerScaleDynamic(DockerScaleBo projectInfo);

    LogTailDTO milogLogtailDO2DTO(MilogLogTailDo milogLogtailDo);

    Result<List<MapDTO>> queryAppByStoreId(Long storeId);

    Result<List<AppTypeTailDTO>> queryAppTailByStoreId(Long storeId);

    Result<List<MilogLogStoreDO>> queryLogStoreByRegionEn(String nameEn);

    Result<List<LogTailDTO>> getTailByStoreId(Long storeId);

    Result<Object> parseScriptTest(MlogParseParam mlogParseParam);

    Result<List<QuickQueryVO>> quickQueryByApp(Long milogAppId);

    void machineIpChange(HeraEnvIpVo heraEnvIpVo);

    Result<QuickQueryVO> queryAppStore(Long appId, Integer platFormCode);
}
