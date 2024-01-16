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
package com.xiaomi.mone.hera.demo.server.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo Configuration
 */
@Configuration
public class DubboConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${dubbo.registry.address}")
    private String dubboRegistryAddress;

    @Value("${dubbo.group}")
    private String dubboProviderGroup;

    /**
     * Dubbo application configuration
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);
        applicationConfig.setQosEnable(false);
        return applicationConfig;
    }

    /**
     * Configure registry center
     */
    @Bean
    public RegistryConfig registryConfig() {

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(dubboRegistryAddress);
        return registryConfig;
    }

    /**
     * Configure registry center
     */
    @Bean
    public ProviderConfig providerConfig() {

        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setGroup(dubboProviderGroup);
        providerConfig.setVersion("1.0");
        providerConfig.setTimeout(1000);
        return providerConfig;
    }

    /**
     * Configuration protocol
     */
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        //Automatic attempt
        protocolConfig.setPort(-1);
        protocolConfig.setTransporter("netty4");
        protocolConfig.setThreadpool("fixed");
        protocolConfig.setThreads(800);
        return protocolConfig;
    }

}