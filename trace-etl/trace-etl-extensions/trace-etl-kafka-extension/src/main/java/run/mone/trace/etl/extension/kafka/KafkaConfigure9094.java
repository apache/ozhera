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
package run.mone.trace.etl.extension.kafka;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.Properties;

public class KafkaConfigure9094 implements KafkaConfigure {

    /**
     * kafka.client.truststore.jks file location
     */
    @NacosValue("${java.security.auth.login.config}")
    private String authLoginConfigLocation;
    @NacosValue("${sasl.mechanism}")
    private String saslMechanism;
    /**
     * kafka.client.truststore.jks file location
     */
    @NacosValue("${ssl.truststore.location}")
    private String sslTruststoreLocation;


    /**
     * username and password in aliyun
     */
    @NacosValue("${kafka.username}")
    private String saslUserName;
    @NacosValue("${kafka.password}")
    private String saslPassword;

    /**
     * kafka_client_jaas_plain.conf file location
     */
    @NacosValue("${java.security.auth.login.config.plain}")
    private String configPlainLocation;
    /**
     * kafka_client_jaas_scram.conf file location
     */
    @NacosValue("${java.security.auth.login.config.scram}")
    private String configScramLocation;

    @NacosValue("${kafka.poll.records}")
    private int kafkaPollRecords;


    private void configureSaslPlain() {
        //如果用-D或者其它方式设置过，这里不再设置
        if (null == System.getProperty("java.security.auth.login.config")) {
            //请注意将XXX修改为自己的路径
            //这个路径必须是一个文件系统可读的路径，不能被打包到jar中
            System.setProperty("java.security.auth.login.config", configPlainLocation);
        }
    }

    private void configureSaslScram() {
        //如果用-D或者其它方式设置过，这里不再设置
        if (null == System.getProperty("java.security.auth.login.config")) {
            //请注意将XXX修改为自己的路径
            //这个路径必须是一个文件系统可读的路径，不能被打包到jar中
            System.setProperty("java.security.auth.login.config", configScramLocation);
        }
    }

    @Override
    public Properties createProducerProperties(MqConfig<ConsumerRecords<String, String>> config) {
        //设置sasl文件的路径,区分plain和scram
        configureSaslPlain();

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getNameSerAddr());

        //接入协议，
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        // scram 方式和plain方式区别
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        //props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");

        //Kafka消息的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        return props;
    }

    @Override
    public Properties createConsumerProperties(MqConfig<ConsumerRecords<String, String>> config) {
        //设置sasl文件的路径,区分plain和scram
        configureSaslPlain();

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getNameSerAddr());

        //接入协议，
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");

        // 设置SASL账号
        if (StringUtils.isNotEmpty(saslUserName)
                && StringUtils.isNotEmpty(saslPassword)) {
            String prefix = "org.apache.kafka.common.security.scram.ScramLoginModule";
            if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                prefix = "org.apache.kafka.common.security.plain.PlainLoginModule";
            }
            String jaasConfig = String.format("%s required username=\"%s\" password=\"%s\";", prefix, saslUserName, saslPassword);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        } else {
            if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                configureSaslPlain();
            } else {
                configureSaslScram();
            }
        }

        // scram 方式和plain方式区别
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        //props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");

        //可更加实际拉去数据和客户的版本等设置此值，默认30s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        //每次poll的最大数量
        //注意该值不要改得太大，如果poll太多数据，而不能在下次poll之前消费完，则会触发一次负载均衡，产生卡顿
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaPollRecords);
        //消息的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //当前消费实例所属的消费组，请在控制台申请之后填写
        //属于同一个组的消费实例，会负载消费消息
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        return props;
    }
}
