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
package org.apache.ozhera.log.manager.service.nacos.impl;

import com.alibaba.nacos.api.config.ConfigService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.nacos.DynamicConfigPublisher;
import org.apache.ozhera.log.model.MilogSpaceData;

import static org.apache.ozhera.log.common.Constant.DEFAULT_GROUP_ID;
import static org.apache.ozhera.log.common.Constant.TAIL_CONFIG_DATA_ID;

/**
 * @author wtt
 * @version 1.0
 * @description namespace Configure push NACOS
 * @date 2021/7/16 10:36
 */
@Slf4j
public class SpaceConfigNacosPublisher implements DynamicConfigPublisher<MilogSpaceData> {

    @Setter
    private ConfigService configService;

    @Override
    public void publish(Long uniqueSpace, MilogSpaceData config) {
        String configJson = gson.toJson(config);
        log.info("write the creation namespace configuration:{}", configJson);
        String dataId = CommonExtensionServiceFactory.getCommonExtensionService().getLogManagePrefix() + TAIL_CONFIG_DATA_ID + uniqueSpace;
        try {
            if (null != configService) {
                if (StringUtils.isBlank(dataId)) {
                    log.error("dataId is null,uniqueSpace:{},config:{}", uniqueSpace, configJson);
                    return;
                }
                configService.publishConfig(dataId, DEFAULT_GROUP_ID, gson.toJson(config));
            } else {
                log.warn("configService is null,uniqueSpace:{},config:{}", uniqueSpace, configJson);
            }
        } catch (Exception e) {
            log.error(String.format("Write the creation namespace configuration...,dataId:%s,data:%s", dataId, configJson), e);
        }
    }

    @Override
    public void remove(String spaceId) {
        String dataId = CommonExtensionServiceFactory.getCommonExtensionService().getLogManagePrefix() + TAIL_CONFIG_DATA_ID + spaceId;
        try {
            if (null != configService) {
                configService.removeConfig(dataId, DEFAULT_GROUP_ID);
            }
        } catch (Exception e) {
            log.error(String.format("Delete log configuration data data exceptions,param：%s", dataId), e);
        }
    }
}
