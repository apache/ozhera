package com.xiaomi.mone.app.service.mq;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.xiaomi.mone.app.api.model.HeraMetaDataMessage;
import com.xiaomi.mone.app.api.model.HeraMetaDataPortModel;
import com.xiaomi.mone.app.api.service.HeraMetaDataService;
import com.xiaomi.mone.app.dao.mapper.HeraMetaDataMapper;
import com.xiaomi.mone.app.model.HeraMetaData;
import com.xiaomi.mone.app.redis.RedisService;
import com.xiaomi.mone.app.util.HeraMetaDataConvertUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/5/4 6:14 PM
 */
@Service
@Slf4j
public class RocketMqHeraMetaDataConsumer {

    @NacosValue(value = "${rocket.mq.hera.metadata.topic}", autoRefreshed = true)
    private String consumerTopic;

    @NacosValue(value = "${rocket.mq.hera.metadata.producer.group}", autoRefreshed = true)
    private String consumerGroup;

    @NacosValue(value = "${rocket.mq.srvAddr}", autoRefreshed = true)
    private String namesrvAddr;

    //默认为空，根据需要配置
    @NacosValue(value = "${rocketmq.ak}", autoRefreshed = true)
    private String ak;

    //默认为空，根据需要配置
    @NacosValue(value = "${rocketmq.sk}", autoRefreshed = true)
    private String sk;

    @Autowired
    private HeraMetaDataService heraMetaDataService;

    @Autowired
    private RedisService redisService;

    private static final String REDIS_DISLOCK_KEY_PREFIX = "hera_metadata_";

    // Maximum waiting time for data synchronization to block
    private static final int SYNC_REDIS_WAIT_DURATION = 3 * 60 * 1000;

    private HeraMetaDataMapper heraMetaDataMapper;

    public RocketMqHeraMetaDataConsumer(HeraMetaDataMapper heraMetaDataMapper) {
        this.heraMetaDataMapper = heraMetaDataMapper;
    }

    private DefaultMQPushConsumer heraMetaDataMQPushConsumer;

    private AtomicBoolean rocketMqStartedStatus = new AtomicBoolean(false);

    private Gson gson = new Gson();

    @PostConstruct
    public void start() throws MQClientException {

        try {
            boolean b = rocketMqStartedStatus.compareAndSet(false, true);
            if (!b) {
                log.error("RocketMqHeraMetaDataConsumer.heraMetaDataMQPushConsumer start failed, it has started!!");
                return;
            }

            log.info("RocketMqHeraMetaDataConsumer.heraMetaDataMQPushConsumer init start!!");
            if (StringUtils.isNotEmpty(ak)
                    && StringUtils.isNotEmpty(sk)) {
                SessionCredentials credentials = new SessionCredentials(ak, sk);
                RPCHook rpcHook = new AclClientRPCHook(credentials);
                heraMetaDataMQPushConsumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
            } else {
                heraMetaDataMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
            }
            heraMetaDataMQPushConsumer.setNamesrvAddr(namesrvAddr);
            heraMetaDataMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

            heraMetaDataMQPushConsumer.subscribe(consumerTopic, "*");
            heraMetaDataMQPushConsumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
                try {
                    list.stream().forEach(it -> {
                        log.info("RocketMqHeraMetaDataConsumer# heraMetaDataMQPushConsumer received message : MsgId: {}, Topic: {} Tags:{}", it.getMsgId(), it.getTopic(), it.getTags());
                        consumeMessage(it);
                    });
                } catch (Exception e) {
                    log.info("RocketMqHeraMetaDataConsumer# heraMetaDataMQPushConsumer message error: {}", e.getMessage(), e);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            });


            log.info("RocketMqHeraMetaDataConsumer# heraMetaDataMQPushConsumer init end!!");

            heraMetaDataMQPushConsumer.start();
            log.info("RocketMqHeraMetaDataConsumer# heraMetaDataMQPushConsumer has started!!");

        } catch (MQClientException e) {
            log.error("RocketMqHeraMetaDataConsumer# heraMetaDataMQPushConsumer start error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void consumeMessage(MessageExt message) {
        log.info("RocketMqHeraMetaDataConsumer# consumeMessage: {} {}", message.getMsgId(), new String(message.getBody()));
        try {
            byte[] body = message.getBody();
            HeraMetaDataMessage heraMetaDataMessage = gson.fromJson(new String(body), HeraMetaDataMessage.class);
            log.info("RocketMqHeraMetaDataConsumer# consumeMessage convert heraMetaDataMessage : {}", heraMetaDataMessage.toString());

            HeraMetaData heraMetaData = HeraMetaDataConvertUtil.messageConvertTo(heraMetaDataMessage);

            if ("insert".equals(heraMetaDataMessage.getOperator())) {
                int availablePort = getAvailablePort(heraMetaDataMessage.getPort());
                if (availablePort > 0) {
                    // Check whether synchronous data blocking is required to prevent repeated data insertion
                    if (waitSyncData()) {
                        // Gets a distributed lock to prevent repeated insertions
                        String key = heraMetaDataMessage.getMetaId() + "_" + heraMetaDataMessage.getHost() + "_" + availablePort;
                        if (redisService.getDisLock(key)) {
                            try {
                                List<HeraMetaData> list = getList(heraMetaDataMessage.getMetaId(), heraMetaDataMessage.getHost(), heraMetaDataMessage.getPort());
                                if (list == null || list.isEmpty()) {
                                    Date date = new Date();
                                    heraMetaData.setCreateTime(date);
                                    heraMetaData.setUpdateTime(date);
                                    heraMetaDataMapper.insert(heraMetaData);
                                }
                            } finally {
                                redisService.del(key);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.error("RocketMqHeraMetaDataConsumer#consumeMessage error:" + ex.getMessage(), ex);
        }
    }

    private int getAvailablePort(HeraMetaDataPortModel port) {
        Class<? extends HeraMetaDataPortModel> aClass = port.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                int o = (int) field.get(port);
                if (o > 0) {
                    return o;
                }
            } catch (Exception e) {
                log.error("Hera meta data Consumer getAvailablePort error : ", e);
            }
        }
        return 0;
    }

    private List<HeraMetaData> getList(Integer metaId, String ip, HeraMetaDataPortModel port) {
        QueryWrapper<HeraMetaData> queryWrapper = new QueryWrapper();
        queryWrapper.eq("meta_id", metaId);
        queryWrapper.eq("host", ip);
        queryWrapper.eq("port -> '$.dubboPort'", port.getDubboPort());
        return heraMetaDataMapper.selectList(queryWrapper);
    }

    /**
     * Do not continue the operation until data synchronization is complete.
     * In this case, data is repeatedly inserted during data synchronization.
     *
     * @return
     */
    private boolean waitSyncData() {
        long startTime = System.currentTimeMillis();
        String isSync = redisService.get(HeraMetaDataConvertUtil.SYNC_DATA_LOCK_REDIS_KEY);
        // default return true
        if (isSync == null) {
            return true;
        }
        while (true) {
            if (!"true".equals(isSync)) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    log.error("Hera meta data Consumer waitSyncData error : ", e);
                }
                if (System.currentTimeMillis() - startTime > SYNC_REDIS_WAIT_DURATION) {
                    log.warn("Hera meta data Consumer waitSyncData timeout!");
                    return true;
                }
                isSync = redisService.get(HeraMetaDataConvertUtil.SYNC_DATA_LOCK_REDIS_KEY);
            } else {
                return true;
            }
        }
    }
}
