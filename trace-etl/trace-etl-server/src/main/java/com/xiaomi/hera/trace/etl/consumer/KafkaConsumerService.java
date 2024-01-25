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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.thrift.TDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "mq.type", havingValue = "kafka")
@Slf4j
public class KafkaConsumerService {

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

        MqConfig<ConsumerRecords<String, String>> config = new MqConfig<>();
        config.setNameSerAddr(nameSerAddr);
        config.setProducerGroup(producerGroup);
        config.setProducerTopicName(esTopicName);

        config.setConsumerGroup(consumerGroup);
        config.setConsumerTopicName(topicName);

        config.setConsumerMethod((records)->{
            enterManager.enter();
            enterManager.getProcessNum().incrementAndGet();
            try {
                List<ProducerRecord<String, String>> producerRecordList = new ArrayList<>();
                for (ConsumerRecord<String, String> message : records) {
                    String traceId = "";
                    try {
                        TSpanData tSpanData = new TSpanData();
                        new TDeserializer(ThriftUtil.PROTOCOL_FACTORY).deserialize(tSpanData, message.value().getBytes(StandardCharsets.ISO_8859_1));
                        traceId = tSpanData.getTraceId();
                        metricsExporterService.parse(tSpanData);
                    } catch (Throwable t) {
                        log.error("consumer message error", t);
                    }
                    producerRecordList.add(new ProducerRecord<>(esTopicName, traceId, message.value()));
                }
                if(producerRecordList.size() > 0) {
                    mq.send(producerRecordList);
                }
                return true;
            } finally {
                enterManager.getProcessNum().decrementAndGet();
            }
        });

        mq.initMq(config);
    }

}
