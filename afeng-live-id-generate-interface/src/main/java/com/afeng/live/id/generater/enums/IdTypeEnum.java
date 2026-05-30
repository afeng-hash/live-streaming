package com.afeng.live.id.generater.enums;

public enum IdTypeEnum {
    USER_ID(1L,"用户id生成策略");

    Long code;
    String desc;

    IdTypeEnum(Long code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Long getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
