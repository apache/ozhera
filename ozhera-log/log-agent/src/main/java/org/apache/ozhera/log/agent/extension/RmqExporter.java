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
import com.google.gson.Gson;
import org.apache.ozhera.tspandata.TSpanData;
import org.apache.ozhera.log.agent.common.HashUtil;
import org.apache.ozhera.log.agent.common.trace.TraceUtil;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.apache.ozhera.log.api.model.msg.LineMessage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
        this.messageQueueList = fetchMessageQueue(this.rmqTopic);
        List<Message> logMessages = new ArrayList<>();
        Map<MessageQueue, List<Message>> messageQueueListMap = new HashMap<>();

        for (LineMessage lineMessage : messageList) {
            if (OPENTELEMETRY_TYPE.equals(lineMessage.getProperties(LineMessage.KEY_MESSAGE_TYPE))) {
                processOpenTelemetryMessage(lineMessage, messageQueueListMap);
            } else {
                processLogMessage(lineMessage, logMessages);
            }
        }

        sendMessagesToQueues(messageQueueListMap);
        sendMessagesToProducer(logMessages);
    }

    private void processOpenTelemetryMessage(LineMessage lineMessage, Map<MessageQueue, List<Message>> messageQueueListMap) {
        byte[] bytes = TraceUtil.toBytes(lineMessage.getMsgBody());
        if (bytes != null) {
            TSpanData tSpanData = TraceUtil.toTSpanData(lineMessage.getMsgBody());
            String appName = tSpanData.getExtra().getServiceName();
            Message message = new Message();
            message.setBody(bytes);
            message.setTopic(this.rmqTopic);

            // Calculate the message queue based on the appName
            MessageQueue messageQueue = calculateMessageQueue(appName);

            messageQueueListMap.putIfAbsent(messageQueue, new ArrayList<>());
            messageQueueListMap.get(messageQueue).add(message);
        }
    }

    private void processLogMessage(LineMessage lineMessage, List<Message> logMessages) {
        Message message = new Message();
        message.setTags(lineMessage.getProperties(LineMessage.KEY_MQ_TOPIC_TAG));
        message.setBody(gson.toJson(lineMessage).getBytes(StandardCharsets.UTF_8));
        message.setTopic(this.rmqTopic);
        logMessages.add(message);
    }

    private MessageQueue calculateMessageQueue(String appName) {
        Integer partitionNumber = 2;
        appName = String.format("p%s%s", ThreadLocalRandom.current().nextInt(partitionNumber), appName);
        return messageQueueList.get(HashUtil.consistentHash(appName, messageQueueList.size()));
    }

    private void sendMessagesToQueues(Map<MessageQueue, List<Message>> messageQueueListMap) {
        try {
            for (Map.Entry<MessageQueue, List<Message>> queueListEntry : messageQueueListMap.entrySet()) {
                List<Message> messages = queueListEntry.getValue();
                if (CollectionUtils.isNotEmpty(messages)) {
                    mqProducer.send(messages, queueListEntry.getKey());
                }
            }
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("OPENTELEMETRY log rocketMQ export error", e);
        }
    }

    private void sendMessagesToProducer(List<Message> logMessages) {
        try {
            if (CollectionUtils.isNotEmpty(logMessages)) {
                mqProducer.send(logMessages);
            }
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("normal rocketMQ export error", e);
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
