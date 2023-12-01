package com.xiaomi.mone.log.stream.job.extension.kafka;

import com.xiaomi.mone.log.stream.job.extension.MQConfig;
import lombok.Data;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 14:45
 */
@Data
public class KafkaConfig extends MQConfig {
    private String consumerGroup;

    private String namesAddr;

    private String userName;

    private String password;
}
