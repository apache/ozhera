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
package com.xiaomi.hera.trace.etl.consumer;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.service.IEnterManager;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.api.service.MQExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import com.xiaomi.hera.trace.etl.util.ThriftUtil;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.thrift.TDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author dingtao
 * @Description
 * @date 2021/9/29 2:47 下午
 */
@Service
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketMQ")
@Slf4j
public class RocketMQConsumerService {

    @Value("${mq.consumer.group}")
    private String consumerGroup;

    @Value("${mq.producer.group}")
    private String producerGroup;

    @NacosValue("${mq.nameseraddr}")
    private String nameSerAddr;

    @Value("${mq.server.topic}")
    private String topicName;

    @Value("${mq.es.topic}")
    private String esTopicName;

    @Resource
    private IEnterManager enterManager;

    @Resource
    private IMetricsParseService metricsExporterService;

    @Autowired
    private MQExtension mq;

    @PostConstruct
    public void takeMessage() throws MQClientException {

        MqConfig<MessageExt> config = new MqConfig<>();
        config.setNameSerAddr(nameSerAddr);
        config.setProducerGroup(producerGroup);
        config.setProducerTopicName(esTopicName);

        config.setConsumerGroup(consumerGroup);
        config.setConsumerTopicName(topicName);

        config.setBatchConsumerMethod((list)->{
            enterManager.enter();
            enterManager.getProcessNum().incrementAndGet();
            try {
                for (MessageExt message : list) {
                    String traceId = "";
                    try {
                        TSpanData tSpanData = new TSpanData();
                        new TDeserializer(ThriftUtil.PROTOCOL_FACTORY).deserialize(tSpanData, message.getBody());
                        traceId = tSpanData.getTraceId();
                        metricsExporterService.parse(tSpanData);
                    } catch (Throwable t) {
                        log.error("consumer message error", t);
                    }
                    mq.sendByTraceId(traceId, message);
                }
                return true;
            } finally {
                enterManager.getProcessNum().decrementAndGet();
            }
        });

        mq.initMq(config);
    }


}
