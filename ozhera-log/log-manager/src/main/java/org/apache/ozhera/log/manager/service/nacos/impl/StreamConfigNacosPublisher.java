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
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.nacos.DynamicConfigPublisher;
import org.apache.ozhera.log.model.MiLogStreamConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static org.apache.ozhera.log.common.Constant.DEFAULT_GROUP_ID;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/7/15 15:14
 */
@Slf4j
public class StreamConfigNacosPublisher implements DynamicConfigPublisher<MiLogStreamConfig> {

    @Setter
    @Getter
    private ConfigService configService;

    @Override
    public synchronized void publish(Long spaceId, MiLogStreamConfig config) {
        if (config == null) {
            return;
        }
        try {
            configService.publishConfig(CommonExtensionServiceFactory.getCommonExtensionService().getSpaceDataId(spaceId), DEFAULT_GROUP_ID, gson.toJson(config));
        } catch (NacosException e) {
            log.error(String.format("Create namespace push data exceptions, parameters：%s", gson.toJson(config)), e);
        }
    }

    @Override
    public void remove(String dataId) {

    }

}
