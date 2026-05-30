package com.afeng.live.common.interfaces.enums;

/**
 * 网关请求头枚举
 */
public enum GatewayHeaderEnum {
    USER_LOGIN_ID("用户id","afeng_gh_user_id");

    String desc;
    String name;

    GatewayHeaderEnum(String desc, String name) {
        this.desc = desc;
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }
}
