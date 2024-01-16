/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
