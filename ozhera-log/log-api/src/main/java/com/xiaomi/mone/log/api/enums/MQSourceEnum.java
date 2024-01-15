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
