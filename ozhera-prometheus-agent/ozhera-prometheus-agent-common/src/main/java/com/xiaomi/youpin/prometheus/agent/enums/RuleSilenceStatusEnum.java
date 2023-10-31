package com.xiaomi.youpin.prometheus.agent.enums;

public enum RuleSilenceStatusEnum implements Base {

    SUCCESS(0, "success"),
    EXPIRED(1, "expired"),
    ;

    private Integer code;
    private String desc;

    RuleSilenceStatusEnum(Integer Code, String desc) {
        this.code = Code;
        this.desc = desc;
    }

    public static RuleSilenceStatusEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (RuleSilenceStatusEnum jobStatus : RuleSilenceStatusEnum.values()) {
            if (code.equals(jobStatus.code)) {
                return jobStatus;
            }
        }
        return null;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
