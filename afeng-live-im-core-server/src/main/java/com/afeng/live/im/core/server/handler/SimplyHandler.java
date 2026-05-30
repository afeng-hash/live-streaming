package com.afeng.live.im.core.server.handler;

import com.afeng.live.im.core.server.common.ImMsg;
import io.netty.channel.ChannelHandlerContext;

public interface SimplyHandler {

    /**
     * 处理消息
     * @param ctx
     * @param msg
     */
    void handler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException;
}
