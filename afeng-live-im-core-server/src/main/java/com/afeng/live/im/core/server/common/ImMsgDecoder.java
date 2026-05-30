package com.afeng.live.im.core.server.common;

import com.afeng.live.im.constants.ImConstants;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 消息解码器
 */
public class ImMsgDecoder extends ByteToMessageDecoder {

    private final int BASE_LENGTH = 2 + 4 + 4;

    @Override
    protected void decode(io.netty.channel.ChannelHandlerContext channelHandlerContext, io.netty.buffer.ByteBuf byteBuf, List<Object> list) throws Exception {
        //bytebuf内容的基本校验，长度校验，magic值校验
        if(byteBuf.readShort() != ImConstants.DEFAULT_MAGIC){
            channelHandlerContext.close();
            return;
        }
        int code = byteBuf.readInt();
        int length = byteBuf.readInt();
        //确保bytebuf剩余消息长度足够
        if (byteBuf.readableBytes() < length){
            channelHandlerContext.close();
            return;
        }
        byte[] body = new byte[length];
        byteBuf.readBytes(body);
        //将bytebuf转换为immsg对象
        ImMsg imMsg = new ImMsg();
        imMsg.setMagic(ImConstants.DEFAULT_MAGIC);
        imMsg.setCode(code);
        imMsg.setLength(length);
        imMsg.setBody(body);
        list.add(imMsg);
    }
}
