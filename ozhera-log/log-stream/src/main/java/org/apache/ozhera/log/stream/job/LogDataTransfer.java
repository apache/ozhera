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
package org.apache.ozhera.log.stream.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.stream.common.LogStreamConstants;
import org.apache.ozhera.log.stream.common.SinkJobEnum;
import org.apache.ozhera.log.stream.job.extension.DefaultLogSendFilter;
import org.apache.ozhera.log.stream.job.extension.MessageLifecycleManager;
import org.apache.ozhera.log.stream.job.extension.MessageSender;
import org.apache.ozhera.log.stream.job.extension.MqMessagePostProcessing;
import org.apache.ozhera.log.stream.sink.SinkChain;
import com.xiaomi.youpin.docean.Ioc;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.ozhera.log.common.Constant.COUNT_NUM;
import static org.apache.ozhera.log.parse.LogParser.TIME_STAMP_MILLI_LENGTH;
import static org.apache.ozhera.log.parse.LogParser.esKeyMap_timestamp;
import static org.apache.ozhera.log.stream.common.LogStreamConstants.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/8/22 15:51
 */

@Slf4j
public class LogDataTransfer {

    private final SinkChain sinkChain;
    private final LogParser logParser;
    private final MessageSender messageSender;
    @Getter
    @Setter
    private SinkJobConfig sinkJobConfig;
    @Setter
    private SinkJobEnum jobType;

    private final AtomicLong sendMsgNumber = new AtomicLong(0);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RateLimiter rateLimiter = RateLimiter.create(180000000);

    private MqMessagePostProcessing messagePostProcessing;

    private LogSendFilter logSendFilter;

    private MessageLifecycleManager messageLifecycleManager;

    public LogDataTransfer(SinkChain sinkChain, LogParser logParser,
                           MessageSender messageSender, SinkJobConfig sinkJobConfig) {
        this.sinkChain = sinkChain;
        this.logParser = logParser;
        this.messageSender = messageSender;
        this.sinkJobConfig = sinkJobConfig;
        String mqPostProcessingBean = sinkJobConfig.getMqType() + LogStreamConstants.postProcessingProviderBeanSuffix;
        this.messagePostProcessing = Ioc.ins().getBean(mqPostProcessingBean);
        this.logSendFilter = Ioc.ins().getBean(DefaultLogSendFilter.class);

        this.messageLifecycleManager = getMessageLifecycleManager();
    }

    private MessageLifecycleManager getMessageLifecycleManager() {
        String factualServiceName = Config.ins().get("message.lifecycle.manager", DEFAULT_MESSAGE_LIFECYCLE_MANAGER);
        return Ioc.ins().getBean(factualServiceName);
    }


    public void handleMessage(String type, String msg, String time) {
        try {
            LineMessage lineMessage = parseLineMessage(msg);

            messageLifecycleManager.beforeProcess(sinkJobConfig, lineMessage);

            Map<String, Object> dataMap = parseMessage(lineMessage);

            messageLifecycleManager.afterProcess(sinkJobConfig, lineMessage, dataMap);

            toSendMessage(dataMap);

            messagePostProcessing.postProcessing(sinkJobConfig, msg);
        } catch (Exception e) {
            log.error(jobType.name() + " parse and send error", e);
            throw new RuntimeException(String.format("handleMessage error,msg:%s", msg), e);
        }
    }

    private void toSendMessage(Map<String, Object> dataMap) throws Exception {
        if (sendMsgNumber.get() % COUNT_NUM == 0 || sendMsgNumber.get() == 1) {
            log.info(jobType.name() + " send msg:{}", dataMap);
        }
        if (SinkJobEnum.NORMAL_JOB == jobType) {
            if (null != dataMap && !sinkChain.execute(dataMap)) {
                sendMessage(dataMap);
            }
        } else {
            sendMessage(dataMap);
        }
    }

    private Map<String, Object> parseMessage(LineMessage lineMessage) {
        String ip = lineMessage.getProperties(LineMessage.KEY_IP);
        Long lineNumber = lineMessage.getLineNumber();
        Map<String, Object> dataMap = logParser.parse(lineMessage.getMsgBody(), ip, lineNumber, lineMessage.getTimestamp(), lineMessage.getFileName());
        putCommonData(dataMap);
        return dataMap;
    }

    private LineMessage parseLineMessage(String msg) throws JsonProcessingException {
        return objectMapper.readValue(msg, LineMessage.class);
    }

    private void putCommonData(Map<String, Object> dataMap) {
        dataMap.putIfAbsent(LOG_STREAM_SPACE_ID, sinkJobConfig.getLogSpaceId());
        dataMap.putIfAbsent(LOG_STREAM_STORE_ID, sinkJobConfig.getLogStoreId());
        dataMap.putIfAbsent(LOG_STREAM_TAIL_ID, sinkJobConfig.getLogTailId());
    }

    private void sendMessage(Map<String, Object> dataMap) throws Exception {
        if (!logSendFilter.sendMessageSwitch(dataMap)) {
            return;
        }
        doSendMessage(dataMap);
    }

    private void doSendMessage(Map<String, Object> m) throws Exception {
        sendMsgNumber.incrementAndGet();
        rateLimiter.acquire();
        checkInsertTimeStamp(m);
        messageSender.send(m);
    }

    public void checkInsertTimeStamp(Map<String, Object> mapData) {
        mapData.putIfAbsent(esKeyMap_timestamp, Instant.now().toEpochMilli());
        Object timeStamp = mapData.get(esKeyMap_timestamp);
        if (timeStamp.toString().length() != TIME_STAMP_MILLI_LENGTH) {
            mapData.put(esKeyMap_timestamp, Instant.now().toEpochMilli());
        }
    }
}
