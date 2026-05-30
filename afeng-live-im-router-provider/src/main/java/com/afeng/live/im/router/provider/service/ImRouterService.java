package com.afeng.live.im.router.provider.service;

import com.afeng.live.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterService {
    /**
     * 按照用户id进行发送消息
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
