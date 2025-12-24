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
package org.apache.ozhera.log.agent.config.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.agent.config.ConfigCenter;
import org.apache.ozhera.log.agent.config.ConfigChangeListener;
import org.apache.ozhera.log.agent.extension.nacos.NacosConfigUtil;

import java.util.concurrent.Executor;

import static org.apache.ozhera.log.common.Constant.DEFAULT_GROUP_ID;

/**
 * @author wtt
 * @date 2025/12/23 15:03
 * @version 1.0
 */
@Slf4j
public class NacosConfigCenter implements ConfigCenter {

    private final ConfigService configService;

    public NacosConfigCenter(String serverAddr) throws Exception {
        this.configService = new NacosConfigUtil(serverAddr).getConfigService();
    }

    @Override
    public String getConfig(String dataId) throws Exception {
        return configService.getConfig(dataId, DEFAULT_GROUP_ID, 3000);
    }

    @Override
    public void addListener(String dataId, ConfigChangeListener listener) {
        try {
            configService.addListener(dataId, DEFAULT_GROUP_ID, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    listener.onChange(configInfo);
                }
            });
        } catch (Exception e) {
            log.error("[NacosConfigCenter] add listener failed", e);
        }
    }
}
