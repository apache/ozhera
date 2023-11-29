package com.xiaomi.mone.log.api.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/28 10:03
 */
@Getter
public enum MQSourceEnum {
    ROCKETMQ(1, "rocketmq"),
    KAFKA(2, "kafka");

    private final Integer code;
    private final String name;


    MQSourceEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String queryName(Integer code) {
        for (MQSourceEnum value : MQSourceEnum.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value.getName();
            }
        }
        return ROCKETMQ.getName();
    }
}
