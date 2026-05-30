package com.afeng.live.im.core.server.service;

import com.afeng.live.im.dto.ImMsgBody;

public interface IRouterHandlerService {
    /**
     * 接收消息
     * @param imMsgBody
     */
    void onReceive( ImMsgBody imMsgBody);

    /**
     * 发送消息给客户端
     * @param imMsgBody
     * @return
     */
    boolean sendMsgToClient(ImMsgBody imMsgBody);
}
