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
import org.apache.ozhera.app.api.message.HeraAppInfoModifyMessage;
import org.apache.ozhera.monitor.dao.HeraAppRoleDao;
import org.apache.ozhera.monitor.service.AppGrafanaMappingService;
import org.apache.ozhera.monitor.service.AppMonitorService;
import org.apache.ozhera.monitor.service.GrafanaService;
import org.apache.ozhera.monitor.service.HeraBaseInfoService;
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gaoxihui
 * @date 2021/7/8 9:56 AM
 */

@Service("heraMonitorMqConsumer")
@Slf4j
public class RocketMqHeraMonitorConsumer {

    @NacosValue(value = "${hera.app.modify.notice.topic}", autoRefreshed = true)
    private String consumerTopic;

    @NacosValue(value = "${hera.app.modify.notice.tag}", autoRefreshed = true)
    private String consumerTag;

    @NacosValue(value = "${hera.app.modify.notice.group}", autoRefreshed = true)
    private String consumerGroup;

    @NacosValue(value = "${rocketmq.namesrv.addr}", autoRefreshed = true)
    private String namesrvAddr;

    @NacosValue("${rocketmq.ak}")
    private String ak;

    @NacosValue("${rocketmq.sk}")
    private String sk;

    private DefaultMQPushConsumer heraMonitorMQPushConsumer;

    @Autowired
    HeraAppRoleDao heraAppRoleDao;

    @Autowired
    GrafanaService grafanaService;

    @Autowired
    AppGrafanaMappingService appGrafanaMappingService;

    @Autowired
    HeraBaseInfoService heraBaseInfoService;

    @Autowired
    AppMonitorService appMonitorService;

    private AtomicBoolean rocketMqStartedStatus = new AtomicBoolean(false);

    public void start() throws MQClientException {

        try {
            boolean b = rocketMqStartedStatus.compareAndSet(false, true);
            if (!b) {
                log.error("RocketMqHeraMonitorConsumer.heraAppMQPushConsumer start failed, it has started!!");
                return;
            }

            log.info("RocketMqHeraMonitorConsumer.heraAppMQPushConsumer init start!!");
            if (StringUtils.isNotEmpty(ak)
                    && StringUtils.isNotEmpty(sk)) {
                SessionCredentials credentials = new SessionCredentials(ak, sk);
                RPCHook rpcHook = new AclClientRPCHook(credentials);
                heraMonitorMQPushConsumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
            } else {
                heraMonitorMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
            }

            heraMonitorMQPushConsumer.setNamesrvAddr(namesrvAddr);
            heraMonitorMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

            heraMonitorMQPushConsumer.subscribe(consumerTopic, consumerTag);

            log.info("Mimonitor#RocketMqHeraMonitorConsumer consumerTopic:{},consumerTag:{},consumerGroup:{}",consumerTopic,consumerTag,consumerGroup);
            heraMonitorMQPushConsumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
                try {
                    list.stream().forEach(it -> {
                        log.info("RocketMqHeraMonitorConsumer#  received message : MsgId: {}, Topic: {} Tags:{}", it.getMsgId(), it.getTopic(), it.getTags());
                        consumeMessage(it);
                    });
                } catch (Exception e) {
                    log.info("RocketMqHeraMonitorConsumer#  message error: {}", e.getMessage(), e);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            });

            log.info("RocketMqHeraMonitorConsumer#  init end!!");

            heraMonitorMQPushConsumer.start();
            log.info("RocketMqHeraMonitorConsumer#  has started!!");

        } catch (MQClientException e) {
            log.error("RocketMqHeraMonitorConsumer#  start error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void consumeMessage(MessageExt message) {

        log.info("RocketMqHeraMonitorConsumer# consumeMessage: {} {}", message.getMsgId(), new String(message.getBody()));

        Gson gson = new Gson();
        try {
            byte[] body = message.getBody();
            HeraAppInfoModifyMessage appModifyMessage = gson.fromJson(new String(body), HeraAppInfoModifyMessage.class);
            log.info("RocketMqHeraMonitorConsumer# consumeMessage convert appModifyMessage : {}", appModifyMessage.toString());

            appMonitorService.heraAppInfoModify(appModifyMessage);

        } catch (Throwable ex) {
            log.error("RocketMqHeraMonitorConsumer#consumeMessage error:" + ex.getMessage(), ex);
        }
    }
}


