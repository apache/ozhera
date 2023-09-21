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
package com.xiaomi.mone.log.agent.export.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.mone.log.agent.common.trace.TraceUtil;
import com.xiaomi.mone.log.agent.export.MsgExporter;
import com.xiaomi.mone.log.api.enums.LogTypeEnum;
import com.xiaomi.mone.log.api.model.msg.LineMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rocketmq Default message sending implementation class
 *
 * @author shanwb
 * @date 2021-07-19
 */
@Slf4j
public class RmqExporter implements MsgExporter {

    private DefaultMQProducer mqProducer;

    private String rmqTopic;

    private Integer batchSize;

    private Gson gson = new Gson();

    private final static String OPENTELEMETRY_TYPE = String.valueOf(
            LogTypeEnum.OPENTELEMETRY.getType());

    private List<MessageQueue> messageQueueList;

    public RmqExporter(DefaultMQProducer mqProducer) {
        this.mqProducer = mqProducer;
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

        List<Message> telTryMessages = new ArrayList<>();
        List<Message> otherMessages = new ArrayList<>();

        for (LineMessage lineMessage : messageList) {
            if (OPENTELEMETRY_TYPE.equals(lineMessage.getProperties(LineMessage.KEY_MESSAGE_TYPE))) {
                byte[] bytes = TraceUtil.toBytes(lineMessage.getMsgBody());
                if (bytes != null) {
                    Message message = new Message();
                    message.setBody(bytes);
                    message.setTopic(this.rmqTopic);
                    telTryMessages.add(message);
                }
            } else {
                Message message = new Message();
                message.setTags(lineMessage.getProperties(LineMessage.KEY_MQ_TOPIC_TAG));
                message.setBody(gson.toJson(lineMessage).getBytes(StandardCharsets.UTF_8));
                message.setTopic(this.rmqTopic);
                otherMessages.add(message);
            }
        }

        try {
            if (CollectionUtils.isNotEmpty(telTryMessages)) {
                if (CollectionUtils.isNotEmpty(messageQueueList) && messageQueueList.size() > 2) {
                    mqProducer.send(telTryMessages, messageQueueList.get(new Random().nextInt(messageQueueList.size())));
                } else {
                    mqProducer.send(telTryMessages);
                }
            }
            if (CollectionUtils.isNotEmpty(otherMessages)) {
                mqProducer.send(otherMessages);
            }
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("RocketMQ export error", e);
        }
    }

    public String getRmqTopic() {
        return rmqTopic;
    }

    public void setRmqTopic(String rmqTopic) {
        this.messageQueueList = fetchMessageQueue(rmqTopic);
        this.rmqTopic = rmqTopic;
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


    @Override
    public void close() {
        //mqProducer multi-topic public and cannot shutdown();
    }


    public List<MessageQueue> fetchMessageQueue(String topicName) {
        try {
            return mqProducer.fetchPublishMessageQueues(topicName);
        } catch (MQClientException e) {
            log.error("fetch queue task error : ", e);
        }
        return new ArrayList<>();
    }

}
