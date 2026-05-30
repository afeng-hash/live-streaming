package com.afeng.live.im.constants;

public enum AppIdEnum {

    AFENG_LIVE_BIZ(10001,"奇遇直播业务");

    int code;
    String desc;

    public int getCode() {
        return code;
    }

    AppIdEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
