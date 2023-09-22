package com.xiaomi.hera.trace.etl.consumer;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.service.IEnterManager;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.api.service.MQExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import com.xiaomi.hera.trace.etl.util.ThriftUtil;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.thrift.TDeserializer;
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
    private String consumerGroup;

    @Value("${mq.rocketmq.producer.group}")
    private String producerGroup;

    @NacosValue("${mq.rocketmq.nameseraddr}")
    private String nameSerAddr;

    @Value("${mq.rocketmq.server.topic}")
    private String topicName;

    @Value("${mq.rocketmq.es.topic}")
    private String esTopicName;

    @Resource
    private IEnterManager enterManager;

    @Resource
    private IMetricsParseService metricsExporterService;

    @Resource
    private Extensions extensions;

    @PostConstruct
    public void takeMessage() throws MQClientException {
        MQExtension<MessageExt> mq = extensions.get("mq");

        MqConfig<MessageExt> config = new MqConfig<>();
        config.setNameSerAddr(nameSerAddr);
        config.setProducerGroup(producerGroup);
        config.setProducerTopicName(esTopicName);

        config.setConsumerGroup(consumerGroup);
        config.setConsumerTopicName(topicName);

        config.setBatchConsumerMethod((list)->{
            enterManager.enter();
            enterManager.getProcessNum().incrementAndGet();
            try {
                for (MessageExt message : list) {
                    String traceId = "";
                    try {
                        TSpanData tSpanData = new TSpanData();
                        new TDeserializer(ThriftUtil.PROTOCOL_FACTORY).deserialize(tSpanData, message.getBody());
                        traceId = tSpanData.getTraceId();
                        metricsExporterService.parse(tSpanData);
                    } catch (Throwable t) {
                        log.error("consumer message error", t);
                    }
                    mq.sendByTraceId(traceId, message);
                }
                return true;
            } finally {
                enterManager.getProcessNum().decrementAndGet();
            }
        });

        mq.initMq(config);
    }


}
