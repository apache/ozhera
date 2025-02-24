package org.apache.ozhera.log.api.enums;

public enum ComparisonTypeEnum {
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    EQUAL_TO("=="),
    LESS_THAN_OR_EQUAL_TO("<="),
    LESS_THAN("<");


    private final String desc;

    ComparisonTypeEnum(String desc) {
        this.desc = desc;
    }
}
