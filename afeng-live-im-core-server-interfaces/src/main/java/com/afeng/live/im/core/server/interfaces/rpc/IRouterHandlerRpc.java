package com.afeng.live.im.core.server.interfaces.rpc;


import com.afeng.live.im.dto.ImMsgBody;

import java.util.List;

/**
 * 专门给router层的服务进行调用的接口
 */
public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     * @param msgJson
     */
    void sendMesg( ImMsgBody msgJson);


    /**
     * 批量发送消息，在直播间
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}
