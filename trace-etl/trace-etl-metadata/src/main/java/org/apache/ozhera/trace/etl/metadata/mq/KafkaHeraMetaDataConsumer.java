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

package org.apache.ozhera.trace.etl.metadata.mq;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.gson.Gson;
import org.apache.ozhera.trace.etl.api.service.HeraMetaDataService;
import org.apache.ozhera.trace.etl.api.service.MQExtension;
import org.apache.ozhera.trace.etl.bo.MqConfig;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaData;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaDataMessage;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaDataPortModel;
import org.apache.ozhera.trace.etl.mapper.HeraMetaDataMapper;
import org.apache.ozhera.trace.etl.service.RedisService;
import org.apache.ozhera.trace.etl.util.convert.HeraMetaDataConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.apache.ozhera.trace.etl.metadata.util.Const;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author dingtao
 */
@Service
@ConditionalOnProperty(name = "mq.type", havingValue = "kafka")
@Slf4j
public class KafkaHeraMetaDataConsumer {

    @Value("${mq.consumer.topic}")
    private String consumerTopic;

    @Value("${mq.consumer.group}")
    private String consumerGroup;

    @NacosValue(value = "${mq.nameseraddr}")
    private String namesrvAddr;


    @Autowired
    private HeraMetaDataService heraMetaDataService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQExtension mq;

    @Autowired
    private HeraMetaDataMapper heraMetaDataMapper;

    private Gson gson = new Gson();

    @PostConstruct
    public void start() throws MQClientException {
        MqConfig<ConsumerRecords<String, String>> config = new MqConfig<>();
        config.setNameSerAddr(namesrvAddr);

        config.setConsumerGroup(consumerGroup);
        config.setConsumerTopicName(consumerTopic);

        config.setConsumerMethod((records)->{
            for (ConsumerRecord<String, String> message : records) {
                consumeMessage(message);
            }
            return true;
        });

        mq.initMq(config);
    }

    private void consumeMessage(ConsumerRecord<String, String> message) {
        try {
            byte[] body = message.value().getBytes(StandardCharsets.ISO_8859_1);
            HeraMetaDataMessage heraMetaDataMessage = gson.fromJson(new String(body), HeraMetaDataMessage.class);
            log.info("KafkaHeraMetaDataConsumer# consumeMessage convert heraMetaDataMessage : {}", heraMetaDataMessage.toString());

            HeraMetaData heraMetaData = HeraMetaDataConvert.INSTANCE.messageToBo(heraMetaDataMessage);

            if ("insert".equals(heraMetaDataMessage.getOperator())) {
                int availablePort = getAvailablePort(heraMetaDataMessage.getPort());
                if (availablePort > 0) {
                    // Check whether synchronous data blocking is required to prevent repeated data insertion
                    if (waitSyncData()) {
                        // Gets a distributed lock to prevent repeated insertions
                        String key = heraMetaDataMessage.getMetaId() + "_" + heraMetaDataMessage.getHost() + "_" + availablePort;
                        if (redisService.getDisLock(key)) {
                            try {
                                List<HeraMetaData> list = getList(heraMetaDataMessage.getMetaId(), heraMetaDataMessage.getHost(), heraMetaDataMessage.getPort());
                                if (list == null || list.isEmpty()) {
                                    Date date = new Date();
                                    heraMetaData.setCreateTime(date);
                                    heraMetaData.setUpdateTime(date);
                                    heraMetaDataMapper.insert(heraMetaData);
                                }
                            } finally {
                                redisService.del(key);
                            }
                        }
                    }
                }
            } else if ("update".equals(heraMetaDataMessage.getOperator())) {
                int availablePort = getAvailablePort(heraMetaDataMessage.getPort());
                if (availablePort > 0) {
                    // Check whether synchronous data blocking is required to prevent repeated data insertion
                    if (waitSyncData()) {
                        // Gets a distributed lock to prevent repeated insertions
                        String key = heraMetaDataMessage.getMetaId() + "_" + heraMetaDataMessage.getHost() + "_" + availablePort;
                        if (redisService.getDisLock(key)) {
                            try {
                                Date date = new Date();
                                heraMetaData.setUpdateTime(date);
                                updateByHostAndPort(heraMetaData);
                            } finally {
                                redisService.del(key);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.error("KafkaHeraMetaDataConsumer#consumeMessage error:" + ex.getMessage(), ex);
        }
    }

    private int getAvailablePort(HeraMetaDataPortModel port) {
        Class<? extends HeraMetaDataPortModel> aClass = port.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                int o = (int) field.get(port);
                if (o > 0) {
                    return o;
                }
            } catch (Exception e) {
                log.error("Hera meta data Consumer getAvailablePort error : ", e);
            }
        }
        return 0;
    }

    private List<HeraMetaData> getList(Integer metaId, String ip, HeraMetaDataPortModel port) {
        QueryWrapper<HeraMetaData> queryWrapper = new QueryWrapper();
        queryWrapper.eq("meta_id", metaId);
        queryWrapper.eq("host", ip);
        queryWrapper.eq("port -> '$.dubboPort'", port.getDubboPort());
        return heraMetaDataMapper.selectList(queryWrapper);
    }

    /**
     * only support dubbo port update concurrently
     */
    private int updateByHostAndPort(HeraMetaData heraMetaData){
        UpdateWrapper<HeraMetaData> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("host", heraMetaData.getHost());
        updateWrapper.eq("port -> '$.dubboPort'", heraMetaData.getPort().getDubboPort());
        return heraMetaDataMapper.update(heraMetaData, updateWrapper);
    }

    /**
     * Do not continue the operation until data synchronization is complete.
     * In this case, data is repeatedly inserted during data synchronization.
     *
     * @return
     */
    private boolean waitSyncData() {
        long startTime = System.currentTimeMillis();
        String isSync = redisService.get(Const.SYNC_DATA_LOCK_REDIS_KEY);
        // default return true
        if (isSync == null) {
            return true;
        }
        while (true) {
            if (!"true".equals(isSync)) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    log.error("Hera meta data Consumer waitSyncData error : ", e);
                }
                if (System.currentTimeMillis() - startTime > Const.SYNC_REDIS_WAIT_DURATION) {
                    log.warn("Hera meta data Consumer waitSyncData timeout!");
                    return true;
                }
                isSync = redisService.get(Const.SYNC_DATA_LOCK_REDIS_KEY);
            } else {
                return true;
            }
        }
    }
}