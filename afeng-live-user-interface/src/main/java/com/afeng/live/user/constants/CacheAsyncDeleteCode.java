package com.afeng.live.user.constants;

import lombok.Data;

/**
 * 枚举类：缓存异步删除标识
 */
public enum CacheAsyncDeleteCode {

    USER_INFO_DELETE(0,"用户基础信息删除"),
    USER_TAG_DELETE(1,"用户标签删除");

    public int getCode() {
        return code;
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

    int code;
    String desc;

    CacheAsyncDeleteCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
