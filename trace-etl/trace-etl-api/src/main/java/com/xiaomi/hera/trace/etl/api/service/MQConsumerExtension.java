package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.trace.etl.bo.MqConfig;


/**
 * @author goodjava@qq.com
 * @date 2023/9/19 16:59
 */
public interface MQConsumerExtension {


    void initMq(MqConfig config);
}
