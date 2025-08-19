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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.ozhera.monitor.service.MetricsContextService;

import java.util.concurrent.atomic.AtomicBoolean;

@Service("businessMetricsContextConsumer")
@Slf4j
public class BusinessMetricsContextConsumer {

    @NacosValue(value = "${business.metrics.context.topic}", autoRefreshed = true)
    private String consumerTopic;

    @NacosValue(value = "${business.metrics.context.tag}", autoRefreshed = true)
    private String consumerTag;

    @NacosValue(value = "${hera.app.modify.notice.group}", autoRefreshed = true)
    private String consumerGroup;

    @NacosValue(value = "${rocketmq.namesrv.addr}", autoRefreshed = true)
    private String namesrvAddr;

    @NacosValue("${rocketmq.ak}")
    private String ak;

    @NacosValue("${rocketmq.sk}")
    private String sk;

    @Autowired
    private MetricsContextService metricService;

    private DefaultMQPushConsumer businessMetricsContextConsumer;

    private AtomicBoolean rocketMqStartedStatus = new AtomicBoolean(false);

    public void start() throws MQClientException {

        try {
            boolean b = rocketMqStartedStatus.compareAndSet(false, true);
            if (!b) {
                log.error("BusinessMetricsContextConsumer start failed, it has started!!");
                return;
            }

            log.info("BusinessMetricsContextConsumer init start!!");
            if (StringUtils.isNotEmpty(ak)
                    && StringUtils.isNotEmpty(sk)) {
                SessionCredentials credentials = new SessionCredentials(ak, sk);
                RPCHook rpcHook = new AclClientRPCHook(credentials);
                businessMetricsContextConsumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
            } else {
                businessMetricsContextConsumer = new DefaultMQPushConsumer(consumerGroup);
            }

            businessMetricsContextConsumer.setNamesrvAddr(namesrvAddr);
            businessMetricsContextConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

            businessMetricsContextConsumer.subscribe(consumerTopic, consumerTag);

            log.info("Mimonitor#BusinessMetricsContextConsumer consumerTopic:{},consumerTag:{},consumerGroup:{}",consumerTopic,consumerTag,consumerGroup);
            businessMetricsContextConsumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
                try {
                    list.stream().forEach(it -> {
                        log.info("BusinessMetricsContextConsumer#  received message : MsgId: {}, Topic: {} Tags:{}", it.getMsgId(), it.getTopic(), it.getTags());
                        consumeMessage(it);
                    });
                } catch (Exception e) {
                    log.info("BusinessMetricsContextConsumer#  message error: {}", e.getMessage(), e);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            });

            log.info("BusinessMetricsContextConsumer#  init end!!");

            businessMetricsContextConsumer.start();
            log.info("BusinessMetricsContextConsumer#  has started!!");

        } catch (MQClientException e) {
            log.error("BusinessMetricsContextConsumer#  start error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void consumeMessage(MessageExt message) {

        log.info("BusinessMetricsContextConsumer# consumeMessage: {} {}", message.getMsgId(), new String(message.getBody()));

        Gson gson = new Gson();
        try {
            String body = new String(message.getBody());
            log.info("BusinessMetricsContextConsumer# consumeMessage convert : {}", body);

            metricService.processMetric(body);

        } catch (Throwable ex) {
            log.error("BusinessMetricsContextConsumer#consumeMessage error:" + ex.getMessage(), ex);
        }
    }
}


