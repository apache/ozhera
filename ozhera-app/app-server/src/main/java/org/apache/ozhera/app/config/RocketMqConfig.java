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
package org.apache.ozhera.app.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.app.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.context.annotation.Bean;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/11 19:42
 */
//@Configuration
@Slf4j
public class RocketMqConfig {

    @NacosValue(value = "${rocket.mq.producer.group}",autoRefreshed = true)
    private String producerGroup;

    @NacosValue(value = "${rocket.mq.srvAddr}", autoRefreshed = true)
    private String nameSrvAddr;

    @Bean
    public DefaultMQProducer getMqProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup, true);
        producer.setNamesrvAddr(nameSrvAddr);
        try {
            producer.start();
            return producer;
        } catch (MQClientException e) {
            log.error("ChannelBootstrap.initMqProducer error, RocketmqConfig: {},nameSrvAddr:{}", producerGroup, nameSrvAddr, e);
            throw new AppException("initMqProducer exception", e);
        }
    }
}
