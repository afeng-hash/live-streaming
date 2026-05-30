package com.afeng.live.im.router.provider.rpc;

import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.router.interfaces.rpc.ImRouterRpc;
import com.afeng.live.im.router.provider.service.ImRouterService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class ImRouterRpcImpl implements ImRouterRpc {

    @Resource
    private ImRouterService imRouterService;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {
        return imRouterService.sendMsg(imMsgBody);
    }

    /**
     * 批量发送消息，群聊场景
     * @param imMsgBodyList
     */
    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {
        imRouterService.batchSendMsg(imMsgBodyList);
    }


}
