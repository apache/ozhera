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
