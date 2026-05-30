package com.afeng.live.framework.redis.starter.keys;

import org.springframework.beans.factory.annotation.Value;


public class RedisKeyBuilder {
    @Value("${spring.application.name}")
    private String applicationName;
    private static final String SPLIT_ITEM = ":";
    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    public String getPrefix() {
        return "afeng:" + applicationName + SPLIT_ITEM;
    }

}
