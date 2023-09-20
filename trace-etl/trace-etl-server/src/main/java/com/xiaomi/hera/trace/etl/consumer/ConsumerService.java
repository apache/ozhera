package com.xiaomi.hera.trace.etl.consumer;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.service.MQConsumerExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import run.mone.docean.spring.extension.Extensions;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author dingtao
 * @Description
 * @date 2021/9/29 2:47 下午
 */
@Service
@Slf4j
public class ConsumerService {

    @Value("${mq.rocketmq.consumer.group}")
    private String group;

    @NacosValue("${mq.rocketmq.nameseraddr}")
    private String nameSerAddr;

    @Value("${mq.rocketmq.server.topic}")
    private String topicName;


    @Resource
    private Extensions extensions;

    @PostConstruct
    public void takeMessage() throws MQClientException {
        MQConsumerExtension extension = extensions.get("mqConsumer");
        MqConfig config = new MqConfig();
        config.setGroup(group);
        config.setNameSerAddr(nameSerAddr);
        config.setTopicName(topicName);
        extension.initMq(config);
    }


}
