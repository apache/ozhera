package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.trace.etl.bo.MqConfig;

import java.util.List;

public interface MQProducerExtension<T> {

    void initMq(MqConfig config);

    void send(T message);

    void send(List<T> messages);
}
