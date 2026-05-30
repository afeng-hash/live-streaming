package com.afeng.live.im.core.server.imclient;

import com.afeng.live.im.core.server.common.ImMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        System.out.println("收到服务端的响应数据 result is " + imMsg);
    }
}
