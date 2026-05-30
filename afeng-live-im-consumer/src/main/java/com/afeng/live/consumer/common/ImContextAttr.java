package com.afeng.live.consumer.common;

import io.netty.util.AttributeKey;

/**
 * 通道绑定的附件
 */
public class ImContextAttr {

    /**
     * 绑定用户id
     */
    public static AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    /**
     * appid
     */
    public static AttributeKey<Integer> APP_ID = AttributeKey.valueOf("appId");
}
