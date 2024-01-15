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

import com.xiaomi.mone.log.api.enums.MQSourceEnum;
import com.xiaomi.mone.log.stream.job.LogDataTransfer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xiaomi.mone.log.utils.DateUtils.getTime;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 14:35
 */
@Slf4j
public class KafkaConsumerRunner implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final KafkaConsumer consumer;

    private final LogDataTransfer handleMessage;

    public KafkaConsumerRunner(KafkaConsumer consumer, LogDataTransfer handleMessage) {
        this.consumer = consumer;
        this.handleMessage = handleMessage;
    }

    @Override
    public void run() {
        try {
            while (!closed.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(1000);
                    //This data must be consumed before the next poll, and the total time taken shall not exceed SESSION_TIMEOUT_MS_CONFIG
                    for (ConsumerRecord<String, String> record : records) {
                        if (StringUtils.equals(record.key(), handleMessage.getSinkJobConfig().getTag())) {
                            log.debug("Thread:{} Consume partition:{} offset:{},message:{}",
                                    Thread.currentThread().getName(), record.partition(),
                                    record.offset(), record.value());
                            String time = getTime();
                            String msg = record.value();
                            handleMessage.handleMessage(MQSourceEnum.KAFKA.getName(), msg, time);
                        }
                    }
                } catch (Exception e) {
                    log.error("kafka consumer error", e);
                }
            }
        } catch (Exception e) {
            log.error("KafkaConsumerRunner send exception", e);
        } finally {
            consumer.close();
        }
    }

    // shutdown hook which can be called from a separate thread
    public void shutdown() {
        closed.set(true);
        consumer.close(20, TimeUnit.SECONDS);
    }
}
