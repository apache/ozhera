package com.xiaomi.mone.log.api.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/28 10:03
 */
@Getter
public enum MQSourceEnum {
    ROCKETMQ(1, "rocketmq", "RocketMQService"),
    KAFKA(2, "kafka", "KafkaService");

    private final Integer code;
    private final String name;

    private final String serviceName;


    MQSourceEnum(Integer code, String name, String serviceName) {
        this.code = code;
        this.name = name;
        this.serviceName = serviceName;
    }

    public static String queryName(Integer code) {
        for (MQSourceEnum value : MQSourceEnum.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value.getName();
            }
        }
        return ROCKETMQ.getName();
    }

    public static MQSourceEnum queryByName(String name) {
        if (name == null || "".equals(name)) {
            return null;
        }
        return Arrays.stream(MQSourceEnum.values()).sequential().filter(sourceEnum -> {
            if (sourceEnum.getName().equals(name)) {
                return true;
            }
            return false;
        }).findFirst().orElse(null);
    }
}
