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

package org.apache.ozhera.prometheus.agent.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.prometheus.agent.alertManagerClient.AlertManagerAliClient;
import org.apache.ozhera.prometheus.agent.alertManagerClient.AlertManagerClient;
import org.apache.ozhera.prometheus.agent.alertManagerClient.AlertManagerVMClient;
import org.apache.ozhera.prometheus.agent.client.Client;
import org.apache.ozhera.prometheus.agent.enums.ClientType;
import org.apache.ozhera.prometheus.agent.operators.vm.VMPrometheusOperator;
import org.apache.ozhera.prometheus.agent.prometheusClient.PrometheusAliClient;
import org.apache.ozhera.prometheus.agent.prometheusClient.PrometheusClient;
import org.apache.ozhera.prometheus.agent.operators.ali.AliPrometheusOperator;
import org.apache.ozhera.prometheus.agent.operators.BasicOperator;
import org.apache.ozhera.prometheus.agent.operators.local.LocalPrometheusOperator;
import org.apache.ozhera.prometheus.agent.prometheusClient.PrometheusVMClient;
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
        } else if (ClientType.VM.getDesc().equals(prometheusAgentClientType)){
            return new VMPrometheusOperator();
        } else {
            return new AliPrometheusOperator();
        }
    }

    @Bean(name = "PrometheusClient")
    public Client PrometheusClient() {
        if (ClientType.LOCAL.getDesc().equals(prometheusAgentClientType)) {
            return new PrometheusClient();
        } else if (ClientType.VM.getDesc().equals(prometheusAgentClientType)){
            return new PrometheusVMClient();
        } else {
            return new PrometheusAliClient();
        }
    }

    @Bean(name = "AlertManagerClient")
    public Client AlertManagerClient() {
        if (ClientType.LOCAL.getDesc().equals(prometheusAgentClientType)) {
            return new AlertManagerClient();
        } else if(ClientType.VM.getDesc().equals(prometheusAgentClientType)) {
            return new AlertManagerVMClient();
        } else {
            return new AlertManagerAliClient();
        }
    }

}