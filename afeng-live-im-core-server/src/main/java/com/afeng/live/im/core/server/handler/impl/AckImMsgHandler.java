package com.afeng.live.im.core.server.handler.impl;

import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.SimplyHandler;
import com.afeng.live.im.core.server.service.IMsgAckCheckService;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import com.afeng.live.im.dto.ImMsgBody;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AckImMsgHandler implements SimplyHandler {

    @Resource
    private IMsgAckCheckService iMsgAckCheckService;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException {
        log.info("收到ack消息：{}", msg);
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null){
            ctx.close();
            throw new IllegalAccessException("attr is error");
        }

        iMsgAckCheckService.doMsgAck(JSON.parseObject(msg.getBody(), ImMsgBody.class));
    }
}
