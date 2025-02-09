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
package org.apache.ozhera.log.manager.service.extension.tail;

import com.google.common.collect.Lists;
import org.apache.ozhera.app.api.model.HeraSimpleEnv;
import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.log.api.enums.OperateEnum;
import org.apache.ozhera.log.api.enums.ProjectTypeEnum;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.model.bo.LogTailParam;
import org.apache.ozhera.log.manager.model.dto.MilogAppEnvDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.model.pojo.MilogMiddlewareConfig;
import org.apache.ozhera.log.manager.service.extension.agent.MilogAgentServiceImpl;
import org.apache.ozhera.log.manager.service.impl.HeraAppEnvServiceImpl;
import org.apache.ozhera.log.manager.service.impl.LogTailServiceImpl;
import org.apache.ozhera.log.manager.service.impl.MilogAppMiddlewareRelServiceImpl;
import org.apache.ozhera.log.manager.service.nacos.FetchStreamMachineService;
import org.apache.ozhera.log.manager.service.nacos.impl.StreamConfigNacosPublisher;
import org.apache.ozhera.log.model.LogtailConfig;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.manager.service.extension.agent.MilogAgentService.DEFAULT_AGENT_EXTENSION_SERVICE_KEY;
import static org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService.DEFAULT_TAIL_EXTENSION_SERVICE_KEY;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/10 17:06
 */
@Service(name = DEFAULT_TAIL_EXTENSION_SERVICE_KEY)
@Slf4j
public class DefaultTailExtensionService implements TailExtensionService {

    @Resource
    private LogTailServiceImpl logTailService;

    @Resource
    private MilogAppMiddlewareRelServiceImpl milogAppMiddlewareRelService;

    @Resource
    private HeraAppEnvServiceImpl heraAppEnvService;

    @Resource(name = DEFAULT_AGENT_EXTENSION_SERVICE_KEY)
    private MilogAgentServiceImpl milogAgentService;

    @Resource
    private MilogLogTailDao milogLogtailDao;

    @Override
    public boolean tailHandlePreprocessingSwitch(MilogLogStoreDO milogLogStore, LogTailParam param) {
        return true;
    }

    @Override
    public boolean bindMqResourceSwitch(MilogLogStoreDO logStore, Integer appType) {
        return Objects.equals(ProjectTypeEnum.MIONE_TYPE.getCode().intValue(), appType);
    }

    @Override
    public boolean bindPostProcessSwitch(Long storeId) {
        return false;
    }

    @Override
    public void postProcessing() {

    }

    @Override
    public void defaultBindingAppTailConfigRel(Long id, Long milogAppId, Long middleWareId, String topicName, Integer batchSendSize) {
        milogAppMiddlewareRelService.defaultBindingAppTailConfigRel(id, milogAppId, middleWareId, topicName, batchSendSize);
    }

    @Override
    public void defaultBindingAppTailConfigRelPostProcess(Long spaceId, Long storeId, Long tailId, Long milogAppId, Long storeMqResourceId) {

    }

    @Override
    public void sendMessageOnCreate(LogTailParam param, MilogLogTailDo mt, Long milogAppId, boolean supportedConsume) {
        /**
         * Send configuration information ---log-agent
         */
        if (param.getCollectionReady()) {
            CompletableFuture.runAsync(() -> logTailService.sengMessageToAgent(milogAppId, mt));
        }
        /**
         * Send final configuration information ---log-stream -- View the log template type, if it is OpenTelemetry logs, only send MQ and do not consume
         */
        if (supportedConsume) {
            logTailService.sengMessageToStream(mt, OperateEnum.ADD_OPERATE.getCode());
        }
    }

    @Override
    public void updateSendMsg(MilogLogTailDo milogLogtailDo, List<String> oldIps, boolean supportedConsume) {
        /**
         * Synchronous log-agent
         */
        if (milogLogtailDo.getCollectionReady()) {
            CompletableFuture.runAsync(() -> milogAgentService.publishIncrementConfig(milogLogtailDo.getId(), milogLogtailDo.getMilogAppId(), milogLogtailDo.getIps()));
        }
        /**
         * Synchronous log-stream If it is OpenTelemetry logs, only send MQ and do not consume it
         */
        if (supportedConsume) {
//            List<MilogAppMiddlewareRel> middlewareRels = milogAppMiddlewareRelDao.queryByCondition(milogLogtailDo.getMilogAppId(), null, milogLogtailDo.getId());
//            createConsumerGroup(milogLogtailDo.getSpaceId(), milogLogtailDo.getStoreId(), milogLogtailDo.getId(), milogMiddlewareConfigDao.queryById(middlewareRels.get(0).getMiddlewareId()), milogLogtailDo.getMilogAppId(), false);
            logTailService.sengMessageToStream(milogLogtailDo, OperateEnum.UPDATE_OPERATE.getCode());
        }
        logTailService.compareChangeDelIps(milogLogtailDo.getId(), milogLogtailDo.getLogPath(), milogLogtailDo.getIps(), oldIps);
    }

    @Override
    public void logTailDoExtraFiled(MilogLogTailDo milogLogtailDo, MilogLogStoreDO logStoreDO, LogTailParam logTailParam) {
        milogLogtailDo.setIps(logTailParam.getIps());
    }

    @Override
    public void logTailConfigExtraField(LogtailConfig logtailConfig, MilogMiddlewareConfig middlewareConfig) {

    }

    @Override
    public void logTailDelPostProcess(MilogLogStoreDO logStoreDO, MilogLogTailDo milogLogtailDo) {

    }

    @Override
    public List<MilogAppEnvDTO> getEnInfosByAppId(AppBaseInfo appBaseInfo, Long milogAppId, Integer deployWay, String machineRoom) {
        List<HeraSimpleEnv> heraSimpleEnvs = null;
        try {
            heraSimpleEnvs = heraAppEnvService.querySimpleEnvAppBaseInfoId(milogAppId.intValue());
        } catch (Exception e) {
            log.error(String.format("query ip error:milogAppId:%s,deployWay:%s", milogAppId, deployWay), e);
        }
        if (CollectionUtils.isNotEmpty(heraSimpleEnvs)) {
            return heraSimpleEnvs.stream().map(envBo -> MilogAppEnvDTO.builder().label(envBo.getName()).value(envBo.getId()).ips(envBo.getIps()).build()).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public boolean decorateTailDTOValId(Integer logType, Integer appType) {
        return true;
    }

    @Override
    public List<String> getStreamMachineUniqueList(Integer projectTypeCode, String motorRoomEn) {
        return Lists.newArrayList();
    }

    @Override
    public String deleteCheckProcessPre(Long id) {
        return StringUtils.EMPTY;
    }

    @Override
    public String validLogPath(LogTailParam param) {
        if (Objects.equals(ProjectTypeEnum.MIONE_TYPE.getCode(), param.getAppType())) {
            // Verify the log file with the same name
            List<MilogLogTailDo> appLogTails = milogLogtailDao.queryByMilogAppAndEnv(param.getMilogAppId(), param.getEnvId());
            for (int i = 0; i < appLogTails.size() && null == param.getId(); i++) {
                if (appLogTails.get(i).getLogPath().equals(param.getLogPath())) {
                    return "The current deployment environment for the file " + param.getLogPath() + " Log collection is configured with the following aliases:" + appLogTails.get(i).getTail();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void publishStreamConfigPostProcess(StreamConfigNacosPublisher streamConfigNacosPublisher, Long spaceId, String motorRoomEn) {

    }

    @Override
    public List<String> fetchStreamUniqueKeyList(FetchStreamMachineService fetchStreamMachineService, Long spaceId, String motorRoomEn) {
        List<String> mioneStreamIpList = fetchStreamMachineService.streamMachineUnique();
        if (CollectionUtils.isEmpty(mioneStreamIpList)) {
            mioneStreamIpList = getStreamMachineUniqueList(null, motorRoomEn);
        }
        return mioneStreamIpList;
    }

}
