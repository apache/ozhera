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
package org.apache.ozhera.log.stream.job.extension.impl;

import com.alibaba.fastjson.JSON;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.mone.es.EsProcessor;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.stream.job.compensate.EsCompensateLoopDTO;
import org.apache.ozhera.log.stream.job.compensate.MqMessageDTO;
import org.apache.ozhera.log.stream.job.extension.CompensateMsgConsume;
import org.apache.ozhera.log.stream.plugin.es.EsPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;

import java.time.Instant;
import java.util.List;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/15 15:56
 */
@Slf4j
public class RocketCompensateMsgConsume implements CompensateMsgConsume {

    private RocketMqMessageProduct rocketMqMessageProduct = new RocketMqMessageProduct();

    @Override
    public void consume(String ak, String sk, String serviceUrl, String topic) {
        log.info("【RocketMqMessageConsume】consumer mq service init");
        String mqGroup = Config.ins().get("rocketmq_group", "hear_log_stream");

        DefaultMQPushConsumer consumer = initDefaultMQPushConsumer(ak, sk, mqGroup, serviceUrl);
        try {
            consumer.subscribe(topic, "");
        } catch (MQClientException e) {
            log.error("【RocketMqMessageConsume】Subscription to Rocket Mq consumption exception", e);
        }
        consumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
            list.stream().forEach(ele -> {
                byte[] body = ele.getBody();
                String str = new String(body);
                log.info("RocketMqMessageConsume.consume:{}", str);
                EsCompensateLoopDTO esCompensateLoopDTO;
                try {
                    esCompensateLoopDTO = GSON.fromJson(str, EsCompensateLoopDTO.class);
                    // Compatible with old data
                    if (esCompensateLoopDTO.getMqMessageDTO() == null) {
                        MqMessageDTO mqMessageDTO = GSON.fromJson(str, MqMessageDTO.class);
                        if (mqMessageDTO.getEsInfo() != null) {
                            esCompensateLoopDTO = new EsCompensateLoopDTO();
                            esCompensateLoopDTO.setMqMessageDTO(mqMessageDTO);
                            esCompensateLoopDTO.setRetryCount(0);
                            esCompensateLoopDTO.setLastRetryTime(System.currentTimeMillis());
                        }
                    }
                } catch (Exception e) {
                    MqMessageDTO mqMessageDTO = GSON.fromJson(str, MqMessageDTO.class);
                    esCompensateLoopDTO = new EsCompensateLoopDTO();
                    esCompensateLoopDTO.setMqMessageDTO(mqMessageDTO);
                    esCompensateLoopDTO.setRetryCount(0);
                    esCompensateLoopDTO.setLastRetryTime(System.currentTimeMillis());
                }

                sendMessageReply(esCompensateLoopDTO);
            });
            return ConsumeOrderlyStatus.SUCCESS;
        });
        try {
            consumer.start();
        } catch (MQClientException e) {
            log.error("【RocketMqMessageConsume】Subscription to Rocket Mq consumption exception", e);
        }
    }

    public DefaultMQPushConsumer initDefaultMQPushConsumer(String ak, String sk, String consumerGroup, String address) {
        DefaultMQPushConsumer defaultMQPushConsumer;
        if (!ak.equals("") && !sk.equals("")) {
            SessionCredentials credentials = new SessionCredentials(ak, sk);
            RPCHook rpcHook = new AclClientRPCHook(credentials);
            defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
        } else {
            defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        }
        defaultMQPushConsumer.setNamesrvAddr(address);
        defaultMQPushConsumer.setConsumeFromWhere(CONSUME_FROM_LAST_OFFSET);
        return defaultMQPushConsumer;
    }

    @Override
    public void consume() {
        String ak = Config.ins().get("rocketmq_ak", "");
        String sk = Config.ins().get("rocketmq_sk", "");
        String serviceUrl = Config.ins().get("rocketmq_service_url", "");
        String topic = Config.ins().get("rocketmq_producer_topic", "");
        this.consume(ak, sk, serviceUrl, topic);
        log.info("compensate consume  message succeed");
    }

    private void sendMessageReply(EsCompensateLoopDTO esCompensateLoopDTO) {
        log.info("Compensate Message content: " + GSON.toJson(esCompensateLoopDTO));
        MqMessageDTO mqMessageDTO = esCompensateLoopDTO.getMqMessageDTO();
        //write directly es no handle
        List<MqMessageDTO.CompensateMqDTO> compensateMqDTOS = mqMessageDTO.getCompensateMqDTOS();
        if (CollectionUtils.isNotEmpty(compensateMqDTOS)) {
            compensateMqDTOS.forEach(compensateMqDTO -> {
                String esIndex = compensateMqDTO.getEsIndex();
                String message = compensateMqDTO.getMsg();
                LinkedTreeMap hashMap = GSON.fromJson(message, new TypeToken<LinkedTreeMap<String, Object>>() {
                }.getType());
                try {
                    Long timeStamp = JSON.parseObject(message).getLong(LogParser.esKeyMap_timestamp);
                    if (null != timeStamp && String.valueOf(timeStamp).length() != LogParser.TIME_STAMP_MILLI_LENGTH) {
                        hashMap.put(LogParser.esKeyMap_timestamp, Instant.now().toEpochMilli());
                    }
                } catch (Exception e) {
                    hashMap.put(LogParser.esKeyMap_timestamp, Instant.now().toEpochMilli());
                }
                // Inject retry count for EsPlugin to pick up if this attempt fails
                hashMap.put("__compensate_retry_count", esCompensateLoopDTO.getRetryCount());

                log.info("mq index timestamp data:{},current timestamp:{}", hashMap.get(LogParser.esKeyMap_timestamp), Instant.now().toEpochMilli());
                EsProcessor esProcessor = EsPlugin.getEsProcessor(mqMessageDTO.getEsInfo(),
                        failedDto -> {
                            log.error("compensate msg store failed again, retryCount:{}", failedDto.getRetryCount());
                            // Increment retry count
                            failedDto.setRetryCount(failedDto.getRetryCount() + 1);
                            failedDto.setLastRetryTime(System.currentTimeMillis());
                            // Re-send to MQ
                            rocketMqMessageProduct.product(failedDto);
                        });
                esProcessor.bulkInsert(esIndex, hashMap);
            });
        }
    }
}
