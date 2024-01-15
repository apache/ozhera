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
package com.xiaomi.mone.log.manager.service.impl;

import com.google.common.collect.Lists;
import com.xiaomi.mone.log.manager.model.dto.DictionaryDTO;
import com.xiaomi.mone.log.manager.model.pojo.MilogAppMiddlewareRel;
import com.xiaomi.mone.log.manager.service.CommonRocketMqService;
import com.xiaomi.mone.log.manager.service.MqConfigService;
import com.xiaomi.mone.log.utils.KafkaUtils;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/29 11:37
 */
@Slf4j
@Service
public class KafkaMqConfigService implements MqConfigService, CommonRocketMqService {

    @Value("$kafka.use.ssl")
    private String kafkaUseSsl;

    @Value("$kafka.sll.location")
    private String kafkaSllLocation;

    @Override
    public MilogAppMiddlewareRel.Config generateConfig(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId, Long exceedId, String name, String source, Long id) {
        MilogAppMiddlewareRel.Config config = new MilogAppMiddlewareRel.Config();

        Properties properties = new Properties();

        if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk) && Objects.equals("true", kafkaUseSsl)) {
            properties.putAll(KafkaUtils.getSslKafkaProperties(nameServer, ak, sk, kafkaSllLocation));
        } else if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk)) {
            properties.putAll(KafkaUtils.getVpc9094KafkaProperties(nameServer, ak, sk));
        } else {
            properties.putAll(KafkaUtils.getDefaultKafkaProperties(nameServer));
        }
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);
        String topicName = generateSimpleTopicName(id, name);
        // create AdminClient
        try (AdminClient adminClient = AdminClient.create(properties)) {
            List<NewTopic> topics = Lists.newArrayList(topicName).stream().map(topic -> new NewTopic(topic, Optional.of(1),
                    Optional.of((short) 1))).collect(Collectors.toList());
            CreateTopicsResult result = adminClient.createTopics(topics);
            // Wait for the theme to be created
            Map<String, KafkaFuture<Void>> values = result.values();
            for (Map.Entry<String, KafkaFuture<Void>> entry : values.entrySet()) {
                try {
                    entry.getValue().get(); // Wait for the creation operation to complete
                    log.info("Topic:{},created successfully.", entry.getKey());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Failed to create topic:{}", entry.getKey(), e);
                }
            }
        } catch (Exception e) {
            log.error("create kafka topic error,topic:{}", topicName, e);
        }
        config.setTopic(topicName);
        config.setPartitionCnt(1);
        return config;
    }

    @Override
    public List<DictionaryDTO> queryExistsTopic(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId) {
        Properties properties = new Properties();

        if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk) && Objects.equals("true", kafkaUseSsl)) {
            properties.putAll(KafkaUtils.getSslKafkaProperties(nameServer, ak, sk, kafkaSllLocation));
        } else if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk)) {
            properties.putAll(KafkaUtils.getVpc9094KafkaProperties(nameServer, ak, sk));
        } else {
            properties.putAll(KafkaUtils.getDefaultKafkaProperties(nameServer));
        }
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);

        // create AdminClient
        try (AdminClient adminClient = AdminClient.create(properties)) {
            // get topic list
            Set<String> topics = getTopicList(adminClient);
            return topics.stream().map(data -> {
                DictionaryDTO<String> dictionaryDTO = new DictionaryDTO<>();
                dictionaryDTO.setLabel(data);
                dictionaryDTO.setValue(data);
                return dictionaryDTO;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("query kafka topic list error", e);
        }
        return null;
    }

    private static Set<String> getTopicList(AdminClient adminClient) throws ExecutionException, InterruptedException {
        // Configure ListTopicsOptions
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(true);
        // Get topic list
        ListTopicsResult topicsResult = adminClient.listTopics(options);
        return topicsResult.names().get();
    }

    @Override
    public List<String> createCommonTagTopic(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId) {
        return null;
    }

    @Override
    public boolean CreateGroup(String ak, String sk, String nameServer) {

        return false;
    }


}
