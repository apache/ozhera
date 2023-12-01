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
        // 设置 Kafka 服务器地址
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);
        //接入协议，
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        // 设置SASL账号
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            String prefix = "org.apache.kafka.common.security.scram.ScramLoginModule";
            if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                prefix = "org.apache.kafka.common.security.plain.PlainLoginModule";
            }
            String jaasConfig = String.format("%s required username=\"%s\" password=\"%s\";", prefix, userName, password);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
        // scram 方式和plain方式区别
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        return props;
    }

    public static Properties getDefaultKafkaProperties(String nameServer) {
        // 设置 Kafka 服务器地址
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        return props;
    }

    public static Properties getSslKafkaProperties(String nameServer, String userName, String password, String sslLocation) {
        String saslMechanism = "PLAIN";
        // 设置 Kafka 服务器地址
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslLocation);
        //根证书store的密码，保持不变
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        //接入协议，目前支持使用SASL_SSL协议接入
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        // 设置SASL账号
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
