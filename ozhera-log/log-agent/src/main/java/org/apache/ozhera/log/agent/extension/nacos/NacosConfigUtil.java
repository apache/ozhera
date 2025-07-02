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
package org.apache.ozhera.log.agent.extension.nacos;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import static org.apache.ozhera.log.common.Constant.DEFAULT_GROUP_ID;
import static org.apache.ozhera.log.common.Constant.DEFAULT_TIME_OUT_MS;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/6/10 16:52
 */
public class NacosConfigUtil {
    private final ConfigService configService;

    public NacosConfigUtil(String nacosAddr) throws NacosException {
        this.configService = ConfigFactory.createConfigService(nacosAddr);
    }

    public String getConfig(String dataId) throws NacosException {
        return configService.getConfig(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
    }
}
