package com.xiaomi.mone.log.api.enums;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:31
 */
public enum LogStorageTypeEnum {

    ELASTICSEARCH,
    DORIS,
    CLICKEHOUSE;

    public static LogStorageTypeEnum queryByName(String name) {
        if (null == name || name.length() == 0) {
            return null;
        }
        for (LogStorageTypeEnum value : values()) {
            if (value.name().equals(name.toUpperCase())) {
                return value;
            }
        }
        return null;
    }
}
