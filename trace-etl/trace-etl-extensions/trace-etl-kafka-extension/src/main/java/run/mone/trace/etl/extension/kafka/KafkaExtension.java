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
package run.mone.trace.etl.extension.kafka;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.service.MQExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

@Service
@ConditionalOnProperty(name = "mq.type", havingValue = "kafka")
@Slf4j
public class KafkaExtension implements MQExtension<ProducerRecord<String, String>, ConsumerRecords<String, String>> {

    /**
     * kafka vpc type
     * value is :
     * vpc-9002
     * vpc-ssl-9003
     * vpc-9004
     */
    @NacosValue("${kafka.vpc.type}")
    private String vpcType;

    @Autowired
    private KafkaConfigure kafkaConfigure;

    private KafkaProducer<String, String> producer;

    private String topic;

    private KafkaConsumer<String, String> consumer;

    @Override
    public void initMq(MqConfig<ConsumerRecords<String, String>> config) {
        log.info("init rocketmq");
        if (StringUtils.isNotEmpty(config.getProducerTopicName())) {
            initProducer(config);
        }
        initConsumer(config);
    }

    private void initProducer(MqConfig<ConsumerRecords<String, String>> config) {
        try {
            log.info("init producer start ...");

            Properties props = kafkaConfigure.createProducerProperties(config);

            props.put(ProducerConfig.ACKS_CONFIG, "0");
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 563840);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

            producer = new KafkaProducer<>(props);

            topic = config.getProducerTopicName();

            log.info("init producer end ...");
        } catch (Throwable ex) {
            log.error("init producer error", ex);
            throw new RuntimeException(ex);
        }
    }

    private void initConsumer(MqConfig<ConsumerRecords<String, String>> config) {
        try {
            // initializing rocketmq consumer
            log.info("init consumer start ...");

            Properties props = kafkaConfigure.createConsumerProperties(config);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

            //构造消息对象，也即生成一个消费实例
            consumer = new KafkaConsumer<>(props);
            //设置消费组订阅的Topic，可以订阅多个
            //如果GROUP_ID_CONFIG是一样，则订阅的Topic也建议设置成一样
            List<String> subscribedTopics = new ArrayList<String>();
            //如果需要订阅多个Topic，则在这里add进去即可
            //每个Topic需要先在控制台进行创建
            subscribedTopics.add(config.getConsumerTopicName());
            consumer.subscribe(subscribedTopics);

            Executors.newSingleThreadExecutor().submit(() -> {
                while (true) {
                    consumer(config);
                }
            });
            log.info("init consumer end ...");
        } catch (Throwable ex) {
            log.error("init error", ex);
            throw new RuntimeException(ex);
        }
    }


     private void consumer(MqConfig<ConsumerRecords<String, String>> config){
         try {
             ConsumerRecords<String, String> records = consumer.poll(1000);
             config.getConsumerMethod().apply(records);
         } catch (Throwable t) {
             log.error("consumer message error , ", t);
         }
     }
    @Override
    public void send(ProducerRecord<String, String> message) {
        send(Collections.singletonList(message));
    }

    @Override
    public void send(List<ProducerRecord<String, String>> messages) {
        try {
            for (ProducerRecord<String, String> message : messages) {
                producer.send(message);
            }
//            producer.flush();
        } catch (Throwable t) {
            log.error("send message error, ", t);
        }
    }

    @Override
    public void sendByTraceId(String traceId, ProducerRecord<String, String> message) {

    }

}
