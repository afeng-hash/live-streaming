package com.afeng.live.im.core.server.service.impl;

import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.service.IMsgAckCheckService;
import com.afeng.live.im.core.server.service.IRouterHandlerService;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class IRouterHandlerServiceImpl implements IRouterHandlerService {

    @Resource
    private IMsgAckCheckService iMsgAckCheckService;

    @Override
    public void onReceive(ImMsgBody imMsgBody) {
        //需要进行消息通知的userId
        Long userId = imMsgBody.getUserId();
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);
        log.info("[IRouterHandlerServiceImpl] 获取用户：{} 的ChannelHandlerContext:{}", userId, ctx);
        if (ctx != null){
            String msgId = UUID.randomUUID().toString();
            imMsgBody.setMsgId(msgId);
            ImMsg msg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));
            ctx.writeAndFlush(msg);
            log.info("[IRouterHandlerServiceImpl] 发送消息给用户：{}", userId);
            //当im服务器推送了消息给客户端，然后我们记录下ack
            iMsgAckCheckService.recordMsgAck(imMsgBody,1);
            iMsgAckCheckService.sendDelayMsg(imMsgBody);
        }

    }

    @Override
    public boolean sendMsgToClient(ImMsgBody imMsgBody) {
        //需要进行消息通知的userId
        Long userId = imMsgBody.getUserId();
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);
        log.info("[IRouterHandlerServiceImpl] 获取用户：{} 的ChannelHandlerContext:{}", userId, ctx);
        if (ctx != null){
            String msgId = UUID.randomUUID().toString();
            imMsgBody.setMsgId(msgId);
            ImMsg msg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));
            ctx.writeAndFlush(msg);
            log.info("[IRouterHandlerServiceImpl] 发送消息给用户：{}", userId);
            return true;
        }
        return false;
    }
}
