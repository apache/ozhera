package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.trace.etl.bo.MqConfig;

import java.util.List;


/**
 * @author goodjava@qq.com
 * @date 2023/9/19 16:59
 */
public interface MQExtension<PRODUCER, CONSUMER> {


    void initMq(MqConfig<CONSUMER> config);

    void send(PRODUCER message);

    void send(List<PRODUCER> messages);

    /**
     * This method hashes the traceID to ensure that the same traceID is sent to a single consumer instance.
     * For example, if it's RocketMQ, messages with the same traceID will be sent to the same MessageQueue;
     * if it's Kafka, messages with the same traceID will be sent to the same partition.
     */
    void sendByTraceId(String traceId, PRODUCER message);
}
