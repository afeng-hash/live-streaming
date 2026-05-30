package com.afeng.live.im.router.interfaces.rpc;

import com.afeng.live.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterRpc {

    /**
     * 发送消息
     * @param  imMsgBody
     * @return
     */
    boolean sendMsg(ImMsgBody imMsgBody);


    /**
     * 批量发送消息
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}
