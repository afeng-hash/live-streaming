package com.afeng.live.common.interfaces.common;

/**
 * mq的消息topic
 */
public class ImCoreServerProviderTopicNames {

    /**
     * 接收im系统发送的业务消息
     */
    public static final String AFENG_LIVE_IM_BIZ_MSG_TOPIC = "afeng-live-im-biz-msg-topic";

    /**
     * 接收im系统发送的ack消息
     */
    public static final String AFENG_LIVE_IM_ACK_MSG_TOPIC = "afeng-live-im-ack-msg-topic";

    /**
     * 用户登录im服务消息
     */
    public static final String IM_ONLINE_TOPIC = "im-online-topic";

    /**
     * 用户下线im服务消息
     */
    public static final String IM_OFFLINE_TOPIC = "im-offline-topic";
}
