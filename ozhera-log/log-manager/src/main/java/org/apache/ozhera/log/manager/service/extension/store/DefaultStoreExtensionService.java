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
package org.apache.ozhera.log.manager.service.extension.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.api.enums.OperateEnum;
import org.apache.ozhera.log.api.model.vo.ResourceUserSimple;
import org.apache.ozhera.log.manager.common.ManagerConstant;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.domain.EsIndexTemplate;
import org.apache.ozhera.log.manager.mapper.MilogEsClusterMapper;
import org.apache.ozhera.log.manager.model.dto.EsInfoDTO;
import org.apache.ozhera.log.manager.model.dto.LogStorageData;
import org.apache.ozhera.log.manager.model.pojo.MilogEsClusterDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogMiddlewareConfig;
import org.apache.ozhera.log.manager.model.vo.LogStoreParam;
import org.apache.ozhera.log.manager.service.impl.MilogMiddlewareConfigServiceImpl;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

import static org.apache.ozhera.log.common.Constant.YES;
import static org.apache.ozhera.log.manager.service.extension.store.StoreExtensionService.DEFAULT_STORE_EXTENSION_SERVICE_KEY;

/**
 * @author wtt
 * @version 1.0
 * @description Store resource binding
 * @date 2023/4/10 16:19
 */
@Service(name = DEFAULT_STORE_EXTENSION_SERVICE_KEY)
@Slf4j
public class DefaultStoreExtensionService implements StoreExtensionService {

    @Resource
    private MilogMiddlewareConfigServiceImpl resourceConfigService;

    @Resource
    private MilogMiddlewareConfigServiceImpl milogMiddlewareConfigService;

    @Resource
    private MilogEsClusterMapper milogEsClusterMapper;

    @Resource
    private EsIndexTemplate esIndexTemplate;

    @Resource
    private MilogLogstoreDao logStoreDao;

    @Resource
    private DorisLogStorageService dorisLogStorageService;

    @Override
    public boolean storeInfoCheck(LogStoreParam param) {
        return false;
    }

    @Override
    public void storeResourceBinding(MilogLogStoreDO storeDO, LogStoreParam storeParam, OperateEnum operateEnum) {
        if (StringUtils.isNotEmpty(storeDO.getEsIndex()) && null != storeDO.getMqResourceId() && null != storeDO.getEsClusterId()) {
            //custom resources operate
            customResources(storeDO, storeParam);
            return;
        }
        bindMqResource(storeDO, storeParam);
        bindStorageResource(storeDO, storeParam);
    }

    private void bindStorageResource(MilogLogStoreDO storeDO, LogStoreParam storeParam) {
        ResourceUserSimple resourceUserConfig = resourceConfigService.userResourceList(storeParam.getMachineRoom(), storeParam.getLogType());
        if (resourceUserConfig.getInitializedFlag()) {
            // select the ES cluster
            storeParam.setEsResourceId(storeParam.getEsResourceId() != null ? storeParam.getEsResourceId() : getDefaultEsClusterId());

            MilogEsClusterDO esClusterDO = milogEsClusterMapper.selectById(storeParam.getEsResourceId());
            LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(esClusterDO.getLogStorageType());
            if (storageTypeEnum == LogStorageTypeEnum.DORIS) {
                return;
            }

            EsInfoDTO esInfo = esIndexTemplate.getEsInfo(storeParam.getEsResourceId(), storeParam.getLogType(), null);
            storeParam.setEsIndex(StringUtils.defaultIfBlank(storeParam.getEsIndex(), esInfo.getIndex()));
            storeDO.setEsClusterId(esInfo.getClusterId());
            storeDO.setEsIndex(storeParam.getEsIndex());
        }
    }

    private Long getDefaultEsClusterId() {
        LambdaQueryWrapper<MilogEsClusterDO> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MilogEsClusterDO::getIsDefault, YES);

        return milogEsClusterMapper.selectList(lambdaQueryWrapper)
                .stream()
                .map(MilogEsClusterDO::getId)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private void bindMqResource(MilogLogStoreDO storeDO, LogStoreParam storeParam) {
        ResourceUserSimple resourceUserConfig = resourceConfigService.userResourceList(storeParam.getMachineRoom(), storeParam.getLogType());
        if (resourceUserConfig.getInitializedFlag() && null == storeParam.getMqResourceId()) {
            MilogMiddlewareConfig milogMiddlewareConfig = milogMiddlewareConfigService.queryMiddlewareConfigDefault(storeParam.getMachineRoom());
            storeDO.setMqResourceId(milogMiddlewareConfig.getId());
            storeParam.setMqResourceId(milogMiddlewareConfig.getId());
        }
    }

    private void customResources(MilogLogStoreDO ml, LogStoreParam command) {
        if (null != command.getMqResourceId()) {
            ml.setMqResourceId(command.getMqResourceId());
        }
        if (null != command.getEsResourceId()) {
            ml.setEsClusterId(command.getEsResourceId());
        }
        if (StringUtils.isNotBlank(command.getEsIndex())) {
            ml.setEsIndex(command.getEsIndex());
        }
    }

    @Override
    public void postProcessing(MilogLogStoreDO storeDO, LogStoreParam cmd, OperateEnum operateEnum) {
        if (isDorisStorage(storeDO)) {
            processDorisStorage(storeDO, cmd, operateEnum);
        }
    }

    private boolean isDorisStorage(MilogLogStoreDO storeDO) {
        MilogEsClusterDO esClusterDO = milogEsClusterMapper.selectById(storeDO.getEsClusterId());
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(esClusterDO.getLogStorageType());
        return storageTypeEnum == LogStorageTypeEnum.DORIS;
    }

    private void processDorisStorage(MilogLogStoreDO ml, LogStoreParam cmd, OperateEnum operateEnum) {
        LogStorageData storageData = LogStorageData.builder()
                .storeId(ml.getId()).build();

        switch (operateEnum) {
            case DELETE_OPERATE:
                deleteDorisTable(ml, storageData);
                break;
            case ADD_OPERATE:
                addDorisTable(cmd, storageData);
                break;
            case UPDATE_OPERATE:
                updateDorisTable(ml, cmd, storageData);
                break;
            default:
                // other operations can be processed according to actual needs
        }

        updateEsIndexIfNeeded(ml, storageData, operateEnum);
    }


    private void deleteDorisTable(MilogLogStoreDO ml, LogStorageData storageData) {
        storageData.setClusterId(ml.getEsClusterId());
        dorisLogStorageService.deleteTable(storageData);
    }


    private void addDorisTable(LogStoreParam cmd, LogStorageData storageData) {
        storageData.setLogType(cmd.getLogType());
        storageData.setClusterId(cmd.getEsResourceId());
        storageData.setLogStoreName(cmd.getLogstoreName());
        storageData.setKeys(cmd.getKeyList());
        storageData.setColumnTypes(cmd.getColumnTypeList());
        dorisLogStorageService.createTable(storageData);
    }

    private void updateDorisTable(MilogLogStoreDO ml, LogStoreParam cmd, LogStorageData storageData) {
        storageData.setUpdateKeys(cmd.getKeyList());
        storageData.setUpdateColumnTypes(cmd.getColumnTypeList());
        storageData.setKeys(ml.getKeyList());
        storageData.setColumnTypes(ml.getColumnTypeList());
        storageData.setLogStoreName(ml.getLogstoreName());
        storageData.setUpdateStoreName(cmd.getLogstoreName());
        dorisLogStorageService.updateTable(storageData);
    }

    private void updateEsIndexIfNeeded(MilogLogStoreDO ml, LogStorageData storageData, OperateEnum operateEnum) {
        if (OperateEnum.DELETE_OPERATE == operateEnum) {
            return;
        }
        String tableName = dorisLogStorageService.buildTableName(storageData.getClusterId(), storageData.getStoreId());
        if (!StringUtils.equals(tableName, ml.getEsIndex())) {
            ml.setEsClusterId(storageData.getClusterId());
            ml.setEsIndex(tableName);
            logStoreDao.updateMilogLogStore(ml);
        }
    }

    @Override
    public boolean sendConfigSwitch(LogStoreParam param) {
        return true;
    }

    @Override
    public void deleteStorePostProcessing(MilogLogStoreDO logStoreD) {

    }

    @Override
    public String getMangerEsLabel() {
        return ManagerConstant.ES_LABEL;
    }

    @Override
    public boolean updateLogStore(MilogLogStoreDO ml) {
        return logStoreDao.updateMilogLogStore(ml);
    }

    @Override
    public boolean isNeedSendMsgType(Integer logType) {
        return true;
    }
}
