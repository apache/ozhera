/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.youpin.prometheus.agent.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.xiaomi.data.push.nacos.NacosNaming;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${nacos.config.addrs}"))
@NacosPropertySource(dataId = "prometheus_agent_open_config", autoRefreshed = true)
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class NacosConfiguration {

    @Value("${nacos.config.addrs}")
    private String nacosAddress;
    @NacosValue("${nacos.username}")
    private String username;
    @NacosValue("${nacos.password}")
    private String password;

    @Bean
    public NacosNaming nacosNaming() {
        NacosNaming nacosNaming = new NacosNaming();
        nacosNaming.setServerAddr(nacosAddress);
        nacosNaming.setUsername(username);
        nacosNaming.setPassword(password);
        nacosNaming.init();
        return nacosNaming;
    }
}