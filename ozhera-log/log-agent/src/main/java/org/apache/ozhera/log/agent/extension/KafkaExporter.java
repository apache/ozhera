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
package org.apache.ozhera.log.agent.extension;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.ozhera.log.agent.common.trace.TraceUtil;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.apache.ozhera.log.agent.extension.nacos.AppPartitionConfigService;
import org.apache.ozhera.log.api.enums.LogTypeEnum;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.tspandata.TSpanData;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.ozhera.log.common.Constant.COMMON_MQ_PREFIX;

@Slf4j
public class KafkaExporter implements MsgExporter {

    private Producer producer;

    private String topic;

    private String tag;

    private boolean isCommonTag;

    private Integer batchSize;

    private Integer maxPartitionPer = 4;

    private Producer topAppMqProducer;

    private AppPartitionConfigService appPartitionConfigService;

    public KafkaExporter(Producer mqProducer, String tag, KafkaService kafkaService, Boolean isInitTopApp) {
        this.producer = mqProducer;
        appPartitionConfigService = AppPartitionConfigService.ins();
        if (isInitTopApp) {
            topAppMqProducer = kafkaService.initTopTalosProducer(appPartitionConfigService.getTopicInfo());
        }
        this.tag = tag;
    }

    @Override
    public void close() {

    }

    @Override
    public void export(LineMessage message) {
        this.export(Lists.newArrayList(message));
    }

    @Override
    public void export(List<LineMessage> messageList) {
        if (messageList.isEmpty()) {
            return;
        }

        List<PartitionInfo> partitions = producer.partitionsFor(topic);

        for (LineMessage message : messageList) {
            ProducerRecord<String, String> record = buildProducerRecord(message, partitions);
            if (record != null) {
                producer.send(record, (RecordMetadata metadata, Exception e) -> {
                    if (null != e) {
                        log.error("send message to kafka error", e);
                    }
                });
            }
        }
    }

    private ProducerRecord<String, String> buildProducerRecord(LineMessage message, List<PartitionInfo> partitions) {
        String messageType = message.getProperties(LineMessage.KEY_MESSAGE_TYPE);
        ProducerRecord<String, String> record = null;

        if (String.valueOf(LogTypeEnum.ORIGIN_LOG.getType()).equals(messageType)) {
            record = new ProducerRecord<>(topic, tag, gson.toJson(message.getMsgBody()));
        } else if (OPENTELEMETRY_TYPE.equals(messageType)) {
            String msgBody = message.getMsgBody();
            TSpanData tSpanData = TraceUtil.toTSpanData(msgBody);

            if (tSpanData != null) {
                byte[] bytes = TraceUtil.toBytes(tSpanData);
                if (bytes != null) {
                    String spanMessage = new String(bytes, StandardCharsets.ISO_8859_1);
                    record = new ProducerRecord<>(topic, tag, spanMessage);

                    String appName = tSpanData.getExtra().getServiceName();
                    if (appName != null) {
                        int number = TopAppPartitioner.getMQNumberByAppName(appName, partitions.size(), maxPartitionPer);
                        int partition = partitions.get(number).partition();
                        record = new ProducerRecord<>(topic, partition, tag, spanMessage);
                    }
                }
            }
        } else {
            record = new ProducerRecord<>(topic, tag, gson.toJson(message));
        }

        return record;
    }

    public void setTopic(String topic) {
        this.topic = topic;
        if (topic.startsWith(COMMON_MQ_PREFIX)) {
            this.isCommonTag = true;
        }
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int batchExportSize() {
        if (null == batchSize || batchSize < 0) {
            return BATCH_EXPORT_SIZE;
        }

        return batchSize;
    }
}
