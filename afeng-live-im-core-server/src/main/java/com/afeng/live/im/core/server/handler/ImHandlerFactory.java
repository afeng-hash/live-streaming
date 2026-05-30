package com.afeng.live.im.core.server.handler;

import com.afeng.live.im.core.server.common.ImMsg;
import io.netty.channel.ChannelHandlerContext;

public interface ImHandlerFactory {
    void doMsgHandler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException;
}
