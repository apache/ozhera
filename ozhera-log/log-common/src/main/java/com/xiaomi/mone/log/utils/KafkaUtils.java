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
package com.xiaomi.mone.log.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 14:07
 */
public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Properties getVpc9094KafkaProperties(String nameServer, String userName, String password) {
        String saslMechanism = "PLAIN";
        // Set Kafka server address
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);
        //access protocol，
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        // Set up SASL account
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            String prefix = "org.apache.kafka.common.security.scram.ScramLoginModule";
            if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                prefix = "org.apache.kafka.common.security.plain.PlainLoginModule";
            }
            String jaasConfig = String.format("%s required username=\"%s\" password=\"%s\";", prefix, userName, password);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
        // The difference between scram mode and plain mode
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        return props;
    }

    public static Properties getDefaultKafkaProperties(String nameServer) {
        // Set Kafka server address
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);
        //Maximum wait time for requests
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //Set the number of client internal retries
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //Set client internal retry interval
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        return props;
    }

    public static Properties getSslKafkaProperties(String nameServer, String userName, String password, String sslLocation) {
        String saslMechanism = "PLAIN";
        // Set Kafka server address
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslLocation);
        //The password of the root certificate store, remains unchanged
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        //Access protocol, currently supports access using SASL SSL protocol
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        // Set up SASL account
        if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
            String prefix = "org.apache.kafka.common.security.scram.ScramLoginModule";
            if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                prefix = "org.apache.kafka.common.security.plain.PlainLoginModule";
            }
            String jaasConfig = String.format("%s required username=\"%s\" password=\"%s\";", prefix, userName, password);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        return props;
    }
}
