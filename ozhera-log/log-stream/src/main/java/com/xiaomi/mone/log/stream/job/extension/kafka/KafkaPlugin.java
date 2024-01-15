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
package com.xiaomi.mone.log.stream.job.extension.kafka;

import com.xiaomi.mone.log.common.Config;
import com.xiaomi.mone.log.common.Constant;
import com.xiaomi.mone.log.stream.common.SinkJobEnum;
import com.xiaomi.mone.log.stream.job.extension.MQPlugin;
import com.xiaomi.mone.log.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Objects;
import java.util.Properties;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 14:11
 */
@Slf4j
public class KafkaPlugin implements MQPlugin {

    public static KafkaConfig buildKafkaConfig(String userName, String password, String clusterInfo,
                                               String topic, String tag, String consumerGroup, SinkJobEnum jobType) {
        KafkaConfig config = new KafkaConfig();
        config.setNamesAddr(clusterInfo);
        config.setUserName(userName);
        config.setPassword(password);
        config.setConsumerGroup(StringUtils.isEmpty(consumerGroup) ? Constant.DEFAULT_CONSUMER_GROUP + tag : consumerGroup);
        if (SinkJobEnum.BACKUP_JOB == jobType) {
            config.setConsumerGroup(Constant.DEFAULT_CONSUMER_GROUP + tag + "_" + BACKUP_PREFIX);
        }
        config.setTopicName(topic);
        config.setTag(tag);
        log.info("[KafkaPlugin.initJob] print consumer config:{}", config);
        return config;
    }

    public static KafkaConsumer<String, String> getKafkaConsumer(KafkaConfig config) {
        return initKafkaConsumer(config);
    }

    public static KafkaConsumer<String, String> initKafkaConsumer(KafkaConfig config) {
        Properties props = new Properties();

        String clusterInfo = config.getNamesAddr();
        String userName = config.getUserName();
        String password = config.getPassword();
        Config ins = Config.ins();
        String kafkaUseSsl = ins.get("kafka.use.ssl", "false");
        String kafkaSllLocation = ins.get("kafka.sll.location", "");

        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password) &&
                Objects.equals("true", kafkaUseSsl)) {
            props.putAll(KafkaUtils.getSslKafkaProperties(clusterInfo, userName, password, kafkaSllLocation));
        } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            props.putAll(KafkaUtils.getVpc9094KafkaProperties(clusterInfo, userName, password));
        } else {
            props.putAll(KafkaUtils.getDefaultKafkaProperties(clusterInfo));
        }
        //The maximum allowed interval between two polls
        //This value can be set more realistically to pull data and customer versions. The default is 30 s.
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        //Be careful not to change this value too much. If too much data is polled and cannot be consumed before the next poll, a load balancing will be triggered, causing lag.
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
        //How messages are deserialized
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //Consumer instances belonging to the same group will load consumer messages
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        //Change hostname verification to empty
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        return new KafkaConsumer<>(props);
    }
}
