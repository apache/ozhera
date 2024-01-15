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

import com.google.common.collect.Lists;
import com.xiaomi.mone.log.stream.job.LogDataTransfer;
import com.xiaomi.mone.log.stream.job.extension.SinkJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 12:28
 */
@Slf4j
public class KafkaSinkJob implements SinkJob {

    private final KafkaConfig kafkaConfig;

    private final KafkaConsumer<String, String> consumer;

    private final LogDataTransfer dataTransfer;

    private KafkaConsumerRunner kafkaConsumerRunner;

    public KafkaSinkJob(KafkaConfig kafkaConfig, KafkaConsumer<String, String> consumer, LogDataTransfer dataTransfer) {
        this.kafkaConfig = kafkaConfig;
        this.consumer = consumer;
        this.dataTransfer = dataTransfer;
    }

    @Override
    public boolean start() throws Exception {
        try {
            consumer.subscribe(Lists.newArrayList(kafkaConfig.getTopicName()));
            kafkaConsumerRunner = new KafkaConsumerRunner(consumer, dataTransfer);
            // start by coroutine
            Thread.ofVirtual().start(kafkaConsumerRunner);
        } catch (Exception e) {
            log.error("start kafka consumer error", e);
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() throws Exception {
        kafkaConsumerRunner.shutdown();
    }
}
