package com.xiaomi.hera.trace.etl.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:36
 */
@Data
public class MqConfig<T> implements Serializable {


    private String nameSerAddr;

    private String consumerGroup;
    private String consumerTopicName;

    private String producerGroup;
    private String producerTopicName;

    private Function<T, Boolean> consumerMethod;
    private Function<List<T>, Boolean> batchConsumerMethod;

    private int maxPollRecords;

}
