package com.xiaomi.youpin.prometheus.agent.enums;


public enum ClientType implements Base{
    LOCAL(0, "local"),
    ALI(1, "ali"),
    ;
    private Integer code;
    private String desc;
    ;

    ClientType(Integer Code, String desc) {
        this.code = Code;
        this.desc = desc;
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
