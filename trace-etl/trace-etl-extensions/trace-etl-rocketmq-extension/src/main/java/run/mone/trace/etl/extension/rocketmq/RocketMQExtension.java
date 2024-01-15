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
package run.mone.trace.etl.extension.rocketmq;

import com.xiaomi.hera.trace.etl.api.service.IEnterManager;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.api.service.MQExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:00
 */
@Service
@ConditionalOnProperty(name = "mq", havingValue = "rocketMQ")
@Slf4j
public class RocketMQExtension implements MQExtension<MessageExt, MessageExt> {

    private Function<List<MessageExt>, Boolean> batchConsumerMethod;

    private DefaultMQProducer producer;

    private String topic;

    private ClientMessageQueue clientMessageQueue;

    @Override
    public void initMq(MqConfig<MessageExt> config) {
        log.info("init rocketmq");
        if(StringUtils.isNotEmpty(config.getProducerTopicName())) {
            initProducer(config);
        }
        initConsumer(config);
    }

    private void initProducer(MqConfig<MessageExt> config){
        try {
            log.info("init producer start ...");
            topic = config.getProducerTopicName();
            producer = new DefaultMQProducer(config.getProducerGroup());
            producer.setNamesrvAddr(config.getNameSerAddr());
            producer.start();

            // init clientMessageQueue
            clientMessageQueue = new ClientMessageQueue(this);
            // Before initializing rocketmq consumer,
            // initialize the local message queue to
            // ensure that the local message queue is available when messages come in
            clientMessageQueue.initFetchQueueTask();
            log.info("init producer end ...");
        } catch (Throwable ex) {
            log.error("init producer error", ex);
            throw new RuntimeException(ex);
        }
    }
    private void initConsumer(MqConfig<MessageExt> config){
        try {
            // initializing rocketmq consumer
            log.info("init consumer start ...");
            batchConsumerMethod = config.getBatchConsumerMethod();
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(config.getConsumerGroup());
            consumer.setNamesrvAddr(config.getNameSerAddr());
            consumer.subscribe(config.getConsumerTopicName(), "*");
            consumer.registerMessageListener(new TraceEtlMessageListener());
            consumer.start();
            log.info("init consumer end ...");
        } catch (Throwable ex) {
            log.error("init error", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void send(MessageExt message) {
        this.send(Collections.singletonList(message));
    }

    @Override
    public void send(List<MessageExt> messages) {
        List<Message> list = new ArrayList<>();
        for (MessageExt message : messages) {
            Message msg = new Message();
            msg.setBody(message.getBody());
            msg.setTopic(topic);
            list.add(msg);
        }
        try {
            producer.send(list);
        } catch (Throwable t) {
            log.error("rocketmq producer send error", t);
        }
    }

    @Override
    public void sendByTraceId(String traceId, MessageExt message) {
        clientMessageQueue.enqueue(traceId, message);
    }

    private class TraceEtlMessageListener implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            if (list == null || list.isEmpty()) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            batchConsumerMethod.apply(list);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


    public List<MessageQueue> fetchMessageQueue() {
        try {
            return this.producer.fetchPublishMessageQueues(topic);
        } catch (MQClientException e) {
            log.error("fetch queue task error : ", e);
        }
        return new ArrayList<>();
    }

    public void send(List<MessageExt> messages, MessageQueue messageQueue) {
        List<Message> list = new ArrayList<>();
        for (MessageExt message : messages) {
            Message msg = new Message();
            msg.setBody(message.getBody());
            msg.setTopic(topic);
            list.add(msg);
        }
        try {
            producer.send(list, messageQueue);
        } catch (Throwable t) {
            log.error("rocketmq producer send error", t);
        }
    }
}
