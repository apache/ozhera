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
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.nacos.DynamicConfigProvider;
import org.apache.ozhera.log.model.MiLogStreamConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static org.apache.ozhera.log.common.Constant.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/7/16 15:27
 */
@Slf4j
public class StreamConfigNacosProvider implements DynamicConfigProvider<MiLogStreamConfig> {

    @Setter
    @Getter
    private ConfigService configService;

    @Override
    public MiLogStreamConfig getConfig(Long spaceId) {
        String rules;
        try {
            if (null == spaceId) {
                rules = configService.getConfig(CommonExtensionServiceFactory.getCommonExtensionService().getLogManagePrefix() + NAMESPACE_CONFIG_DATA_ID, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
            } else {
                rules = configService.getConfig(CommonExtensionServiceFactory.getCommonExtensionService().getSpaceDataId(spaceId), DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);

            }
            log.info("The NACOS query log is initially configured：{}", rules);
            if (StringUtils.isNotEmpty(rules)) {
                return gson.fromJson(rules, MiLogStreamConfig.class);
            }
        } catch (Exception e) {
            log.error("Query namespace configuration data data exceptions", e);
        }
        return null;
    }

    public MiLogStreamConfig getConfig(String appName, String dataId) {
        String rules = null;
        try {
            rules = configService.getConfig(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
            if (StringUtils.isNotEmpty(rules)) {
                return gson.fromJson(rules, MiLogStreamConfig.class);
            }
        } catch (Exception e) {
            log.error(String.format("Query namespace configuration data data exceptions, parameters:%s", dataId), e);
        }
        return null;
    }
}
