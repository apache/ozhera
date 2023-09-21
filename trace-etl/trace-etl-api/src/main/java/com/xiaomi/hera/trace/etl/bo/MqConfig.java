package com.xiaomi.hera.trace.etl.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:36
 */
@Data
public class MqConfig<T> implements Serializable {


    private String group;

    private String nameSerAddr;

    private String topicName;

    private Function<T, Boolean> consumerMethod;

}
