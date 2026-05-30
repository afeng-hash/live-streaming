package com.afeng.live.msg.provider.consumer.handler;

import com.afeng.live.im.dto.ImMsgBody;

public interface MessageHandler {

    /**
     * 接收到消息处理逻辑
     * @param imMsgBody
     */
    void onMsgReceive(ImMsgBody imMsgBody);
}
