package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.trace.etl.bo.MqConfig;

import java.util.List;

public interface MQProducerExtension<T> {

    void initMq(MqConfig config);

    void send(T message);

    void send(List<T> messages);

    /**
     * This method hashes the traceID to ensure that the same traceID is sent to a single consumer instance.
     * For example, if it's RocketMQ, messages with the same traceID will be sent to the same MessageQueue;
     * if it's Kafka, messages with the same traceID will be sent to the same partition.
     */
    void sendByTraceId(String traceId, T message);
}
