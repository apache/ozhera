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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.youpin.prometheus.agent.alertManagerClient.AlertManagerAliClient;
import com.xiaomi.youpin.prometheus.agent.alertManagerClient.AlertManagerClient;
import com.xiaomi.youpin.prometheus.agent.client.Client;
import com.xiaomi.youpin.prometheus.agent.enums.ClientType;
import com.xiaomi.youpin.prometheus.agent.prometheusClient.PrometheusAliClient;
import com.xiaomi.youpin.prometheus.agent.prometheusClient.PrometheusClient;
import com.xiaomi.youpin.prometheus.agent.operators.ali.AliPrometheusOperator;
import com.xiaomi.youpin.prometheus.agent.operators.BasicOperator;
import com.xiaomi.youpin.prometheus.agent.operators.local.LocalPrometheusOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/26 15:10
 */

@Configuration
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class ClientConfiguration {

    //default local
    @NacosValue("${prometheus-agent.client.type:local}")
    private String prometheusAgentClientType;

    @Bean
    public BasicOperator prometheusOperator() {
        if (ClientType.LOCAL.getDesc().equals(prometheusAgentClientType)) {
            return new LocalPrometheusOperator();
        } else {
            return new AliPrometheusOperator();
        }
    }

    @Bean(name = "PrometheusClient")
    public Client PrometheusClient() {
        if (ClientType.LOCAL.getDesc().equals(prometheusAgentClientType)) {
            return new PrometheusClient();
        } else {
            return new PrometheusAliClient();
        }
    }

    @Bean(name = "AlertManagerClient")
    public Client AlertManagerClient() {
        if (ClientType.LOCAL.getDesc().equals(prometheusAgentClientType)) {
            return new AlertManagerClient();
        } else {
            return new AlertManagerAliClient();
        }
    }

}
