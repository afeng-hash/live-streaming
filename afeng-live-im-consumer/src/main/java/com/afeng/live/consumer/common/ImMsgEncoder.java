package com.afeng.live.consumer.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 处理消息的编码器
 */
public class ImMsgEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        byteBuf.writeShort(imMsg.getMagic());
        byteBuf.writeInt(imMsg.getCode());
        byteBuf.writeInt(imMsg.getLength());
        byteBuf.writeBytes(imMsg.getBody());
//        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
