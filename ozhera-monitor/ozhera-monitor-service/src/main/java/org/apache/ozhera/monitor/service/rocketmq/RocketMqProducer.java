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

package org.apache.ozhera.monitor.service.rocketmq;

import com.alibaba.fastjson.JSON;
import org.apache.ozhera.monitor.service.rocketmq.model.HeraAppMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author gaoxihui
 * @date 2022/3/9 4:19 下午
 */
@Slf4j
@Service
public class RocketMqProducer {

    @Value("${rocketmq.topic.hera.app}")
    private String heraAppTopic;

    @Value("${rocketmq.tag.hera.app}")
    private String heraAppTag;


    @Autowired
    @Qualifier("defaultMQProducer")
    private DefaultMQProducer producer;

    public void pushHeraAppMsg(HeraAppMessage heraAppMessage) {

        Message msg = new Message(heraAppTopic, heraAppTag, JSON.toJSONString(heraAppMessage).getBytes());
        try {
            producer.send(msg);
            log.info("pushHeraAppMsg send rocketmq message : {}", heraAppMessage.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("pushHeraAppMsg error: " + e.getMessage(), e);
        }
    }
}
