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
package org.apache.ozhera.app.service.mq;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ozhera.app.api.message.HeraAppInfoModifyMessage;
import org.apache.ozhera.app.api.message.HeraAppModifyType;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.dao.HeraAppRoleDao;
import org.apache.ozhera.app.dao.HeraBaseInfoDao;
import org.apache.ozhera.app.model.HeraAppBaseInfo;
import org.apache.ozhera.app.model.HeraAppRole;
import org.apache.ozhera.app.service.impl.HeraAppBaseInfoService;
import org.apache.ozhera.app.service.mq.model.HeraAppMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @date 2021/7/8 9:56 AM
 */

@Service("rocketMqConsumerHera")
@Slf4j
public class RocketMqHeraAppConsumer {

    @NacosValue(value = "${rocket.mq.hera.app.topic}",autoRefreshed = true)
    private String consumerTopic;

    @NacosValue(value = "${rocket.mq.hera.app.tag}",autoRefreshed = true)
    private String consumerTag;

    @NacosValue(value = "${rocket.mq.producer.group}",autoRefreshed = true)
    private String consumerGroup;

    @NacosValue(value = "${rocket.mq.srvAddr}", autoRefreshed = true)
    private String namesrvAddr;

    //默认为空，根据需要配置
    @NacosValue(value = "${rocketmq.ak}", autoRefreshed = true)
    private String ak;

    //默认为空，根据需要配置
    @NacosValue(value = "${rocketmq.sk}", autoRefreshed = true)
    private String sk;

    //stop mq consumer
    @NacosValue(value = "${stop.mq:false}", autoRefreshed = true)
    private Boolean stopMq;

    @NacosValue(value = "${stop.mq.message.plat.code:}", autoRefreshed = true)
    private String mqStopPlatCode;

    private DefaultMQPushConsumer heraAppMQPushConsumer;

    @Autowired
    HeraBaseInfoDao heraBaseInfoDao;

    @Autowired
    HeraAppRoleDao heraAppRoleDao;

    @Autowired
    RocketMqHeraAppProducer mqHeraAppProducer;

    @Autowired
    HeraAppBaseInfoService heraBaseInfoService;

    private AtomicBoolean rocketMqStartedStatus = new AtomicBoolean(false);

    @PostConstruct
    public void start() throws MQClientException {

        try {
            boolean b = rocketMqStartedStatus.compareAndSet(false, true);
            if (!b) {
                log.error("RocketMqHeraAppConsumer.heraAppMQPushConsumer start failed, it has started!!");
                return;
            }

            log.info("RocketMqHeraAppConsumer.heraAppMQPushConsumer init start!!");
            if (StringUtils.isNotEmpty(ak)
                    && StringUtils.isNotEmpty(sk)) {
                SessionCredentials credentials = new SessionCredentials(ak, sk);
                RPCHook rpcHook = new AclClientRPCHook(credentials);
                heraAppMQPushConsumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
            } else {
                heraAppMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
            }
            heraAppMQPushConsumer.setNamesrvAddr(namesrvAddr);
            heraAppMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);


            heraAppMQPushConsumer.subscribe(consumerTopic, consumerTag);
            log.info("HeraApp#RocketMqHeraAppConsumer consumerTopic:{},consumerTag:{},consumerGroup:{}",consumerTopic,consumerTag,consumerGroup);
            heraAppMQPushConsumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
                try {
                    list.stream().forEach(it -> {
                        log.info("RocketMqHeraAppConsumer# heraAppMQPushConsumer received message : MsgId: {}, Topic: {} Tags:{}", it.getMsgId(), it.getTopic(), it.getTags());
                        consumeMessage(it);
                    });
                } catch (Exception e) {
                    log.info("RocketMqHeraAppConsumer# heraAppMQPushConsumer message error: {}", e.getMessage(), e);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            });


            log.info("RocketMqHeraAppConsumer# heraAppMQPushConsumer init end!!");

            heraAppMQPushConsumer.start();
            log.info("RocketMqHeraAppConsumer# heraAppMQPushConsumer has started!!");

        } catch (MQClientException e) {
            log.error("RocketMqHeraAppConsumer# heraAppMQPushConsumer start error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void consumeMessage(MessageExt message) {
        log.info("RocketMqHeraAppConsumer# consumeMessage: {} {}", message.getMsgId(), new String(message.getBody()));
        try {
            byte[] body = message.getBody();
            HeraAppMessage heraAppMessage = new Gson().fromJson(new String(body), HeraAppMessage.class);
            log.info("RocketMqHeraAppConsumer# consumeMessage convert heraAppMessage : {}", heraAppMessage.toString());

            HeraAppBaseInfo changeHeraApp = heraAppMessage.baseInfo();


            if(stopMq){

                log.info("Mq consumer stop ...");
                if(StringUtils.isBlank(mqStopPlatCode)){
                    log.info("mqStopPlatCode is blank, stop all mq message access db!");
                    return;
                }


                List<Integer> platCodes = Arrays.stream(mqStopPlatCode.split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                if(CollectionUtils.isEmpty(platCodes)){
                    log.info("platCodes is empty, stop all mq message access db!");
                    return;
                }

                if(platCodes.contains(changeHeraApp.getPlatformType())){
                    log.info("stop mq access db by plat code : " + changeHeraApp.getPlatformType() + " ; config plat codes " + platCodes);
                    return;
                }

            }


            HeraAppBaseInfo origHeraApp = matchExistHeraApp(heraAppMessage.baseInfo());

            if (heraAppMessage.getDelete() != null && heraAppMessage.getDelete().intValue() == 1) {

                if(origHeraApp == null){
                    log.info("RocketMqHeraAppConsumer# delete hera app, no db data found! heraAppMessage : {}",heraAppMessage.toString());
                    return;
                }

                heraBaseInfoService.delById(origHeraApp.getId());
                log.info("RocketMqHeraAppConsumer# delete hera app info record:{}",heraAppMessage.toString());
                sendHeraAppModify(heraAppMessage.baseInfo(),origHeraApp,HeraAppModifyType.delete);
                return;
            }

            if(origHeraApp == null){
                int create = heraBaseInfoService.create(changeHeraApp);
                String result = create == 1 ? "success!" : "fail!";
                log.info("RocketMqHeraAppConsumer#create heraAppBaseInfo : {}, result:{}", changeHeraApp.toString(),result);
            }else{
                changeHeraApp.setId(origHeraApp.getId());
                changeHeraApp.setStatus(0);
                int update = heraBaseInfoService.update(changeHeraApp);
                String result = update == 1 ? "success!" : "fail!";
                log.info("RocketMqHeraAppConsumer#update heraAppBaseInfo : {}, result:{}", changeHeraApp.toString(),result);
            }

            saveOrUpdateHeraAppRole(heraAppMessage.getJoinedMembers(), heraAppMessage.getId(), heraAppMessage.getPlatformType());

            sendHeraAppModify(heraAppMessage.baseInfo(),origHeraApp,origHeraApp == null ? HeraAppModifyType.create : HeraAppModifyType.update);

        } catch (Throwable ex) {
            log.error("RocketMqHeraAppConsumer#consumeMessage error:" + ex.getMessage(), ex);
        }
    }

    private HeraAppBaseInfo matchExistHeraApp(HeraAppBaseInfo heraAppBaseInfo){
        HeraAppBaseInfoModel queryInfo = new HeraAppBaseInfoModel();
        queryInfo.setBindId(heraAppBaseInfo.getBindId());
        queryInfo.setPlatformType(heraAppBaseInfo.getPlatformType());
        queryInfo.setStatus(0);
        List<HeraAppBaseInfo> query = heraBaseInfoService.query(queryInfo, null, null);
        if(CollectionUtils.isEmpty(query)){
            return null;
        }

        if (query.size() > 1) {
            log.error("matchExistHeraApp#duplicate heraBaseInfo : {}", new Gson().toJson(query));
        }

        return query.get(0);
    }

    private void saveOrUpdateHeraAppRole(List<String> membersP, String appId, Integer platFormType) {

        log.info("RocketMqHeraAppConsumer#saveOrUpdateHeraAppRole appId:{},platFormType:{},members:{}", appId, platFormType, membersP);
        if (CollectionUtils.isEmpty(membersP)) {
            return;
        }

        List<String> members = membersP.stream().distinct().collect(Collectors.toList());

        log.info("check members size === " + members.size());

        if(members.size() > 100){
            log.info("more than 100 members found from mq info by appId : {} and platform_type : {}, update members stop! ", appId, platFormType);
            return;
        }


        HeraAppRole role = new HeraAppRole();
        role.setRole(0);
        role.setAppId(appId);
        role.setAppPlatform(platFormType);
        role.setStatus(0);
//        role.setUser(member);

        Long count = heraAppRoleDao.count(role);
        if(count != null && count.intValue() > 100){
            log.info("more than 100 members has existed in db found by appId : {} and platform_type : {}, update members stop! ", appId, platFormType);
            return;
        }

        List<HeraAppRole> query = heraAppRoleDao.query(role, null, 2000);

        if (CollectionUtils.isEmpty(query)) {

            List<HeraAppRole> roles = members.stream().filter(a -> org.apache.commons.lang3.StringUtils.isNotBlank(a)).map(t -> {
                HeraAppRole r = new HeraAppRole();
                r.setRole(0);
                r.setAppId(appId);
                r.setAppPlatform(platFormType);
                r.setStatus(0);
                r.setUser(t);
                return r;
            }).collect(Collectors.toList());

            heraAppRoleDao.batchCreate(roles);

            return;
        }

        /**
         * 删除本次参数不包含的成员（需先进行此步骤）
         */
        List<HeraAppRole> delRoles = query.stream().filter(t -> !members.contains(t.getUser())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(delRoles)) {
            delRoles.forEach(t -> {
                heraAppRoleDao.delById(t.getId());
            });
        }


        /**
         * 添加本次新增的成员
         */
        members.removeAll(query.stream().map(t -> t.getUser()).collect(Collectors.toList()));

        if (CollectionUtils.isNotEmpty(members)) {
            members.forEach(t -> {

                if (!StringUtils.isBlank(t)) {
                    HeraAppRole roleAdd = new HeraAppRole();
                    roleAdd.setRole(0);
                    roleAdd.setAppId(appId);
                    roleAdd.setAppPlatform(platFormType);
                    roleAdd.setStatus(0);
                    roleAdd.setUser(t);
                    heraAppRoleDao.create(roleAdd);
                }

            });
        }
    }

    /**
     * hera app 发布原数据变更消息通知
     * @param changeInfo 本次变更的hera app信息
     * @param origInfo 原有hera app信息
     */
    private void sendHeraAppModify(HeraAppBaseInfo changeInfo,HeraAppBaseInfo origInfo,HeraAppModifyType modifyType){

        HeraAppInfoModifyMessage modifyMsg = new HeraAppInfoModifyMessage();
        modifyMsg.setModifyType(modifyType);
        BeanUtils.copyProperties(changeInfo,modifyMsg);
        modifyMsg.setAppId(Integer.valueOf(changeInfo.getBindId()));
        if(origInfo != null){
            modifyMsg.setId(origInfo.getId());
        }
        if(HeraAppModifyType.update.equals(modifyType)){
            modifyMsg.setIsNameChange(!changeInfo.getAppName().equals(origInfo.getAppName()) ? true : false);
            modifyMsg.setIsIamTreeIdChange(!changeInfo.getIamTreeId().equals(origInfo.getIamTreeId()) ? true : false);
            modifyMsg.setIsIamTreeTypeChange(changeInfo.getIamTreeType() != null && !changeInfo.getIamTreeType().equals(origInfo.getIamTreeType()) ? true : false);
            modifyMsg.setIsPlatChange(!changeInfo.getPlatformType().equals(origInfo.getPlatformType()) ? true : false);
        }

        mqHeraAppProducer.pushHeraAppMsg(modifyMsg);

    }

}
