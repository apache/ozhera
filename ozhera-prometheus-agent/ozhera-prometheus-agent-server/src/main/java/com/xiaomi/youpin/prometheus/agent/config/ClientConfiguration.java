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
