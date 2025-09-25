package org.apache.ozhera.log.api.enums;

import lombok.Getter;

/**
 * @author wtt
 * @date 2025/9/15 9:58
 * @version 1.0
 */
@Getter
public enum FilterIdEnum {
    FILTER_SPACE_ID(1, "space"),
    FILTER_STORE_ID(2, "store"),
    FILTER_TAIL_ID(3, "tail");
    final int code;
    final String desc;

    FilterIdEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
