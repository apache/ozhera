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
package org.apache.ozhera.app.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.xiaomi.data.push.nacos.NacosConfig;
import com.xiaomi.data.push.nacos.NacosNaming;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @description
 * @date 2022/10/29 11:45
 */
@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${nacos.config.addrs}"))
@NacosPropertySource(dataId = "hera_app_config", autoRefreshed = true)
public class NacosConfiguration {

    @Value("${nacos.config.addrs}")
    private String nacosAddress;

    @Bean
    public NacosConfig getNacosConfig() {
        NacosConfig config = new NacosConfig();
        config.setServerAddr(nacosAddress);
        config.init();
        return config;
    }

    @Bean
    public NacosNaming getNacosNaming() {
        NacosNaming nacosNaming = new NacosNaming();
        nacosNaming.setServerAddr(nacosAddress);
        nacosNaming.init();
        return nacosNaming;
    }
}
