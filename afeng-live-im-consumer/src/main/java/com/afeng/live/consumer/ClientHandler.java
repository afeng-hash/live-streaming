package com.afeng.live.consumer;


import com.afeng.live.consumer.common.ImMsg;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        byte[] body = imMsg.getBody();
        String data = new String(body);
        ImMsgBody imMsgBody = JSON.parseObject(data, ImMsgBody.class);
        System.out.println("收到服务端的响应数据 result is " + imMsgBody.getData());

        if (imMsg.getCode() == ImMsgCodeEnum.IM_BIZ_MSG.getCode()){
            //发送ack消息包给服务端
            ImMsgBody resBody = new ImMsgBody();
            resBody.setAppId(imMsgBody.getAppId());
            resBody.setUserId(imMsgBody.getUserId());
            resBody.setMsgId(imMsgBody.getMsgId());
            resBody.setData("服务端已收到你的消息");
            ImMsg resMsg = ImMsg.build(ImMsgCodeEnum.IM_ACK_MSG.getCode(), JSON.toJSONString(resBody));
//            ctx.writeAndFlush(resMsg);
        }
    }
}
