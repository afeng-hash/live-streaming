package com.afeng.live.im.core.server.rpc;

import com.afeng.live.im.core.server.interfaces.rpc.IRouterHandlerRpc;
import com.afeng.live.im.core.server.service.IRouterHandlerService;
import com.afeng.live.im.dto.ImMsgBody;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class IRouterHandlerRpcImpl implements IRouterHandlerRpc {

    @Resource
    private IRouterHandlerService iRouterHandlerService;

    @Override
    public void sendMesg( ImMsgBody imMsgBody) {
        iRouterHandlerService.onReceive(imMsgBody);
    }


    /**
     * 批量发送消息
     * @param imMsgBodyList
     */
    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {

        for (ImMsgBody imMsgBody : imMsgBodyList) {
            iRouterHandlerService.onReceive(imMsgBody);
        }
    }
}
