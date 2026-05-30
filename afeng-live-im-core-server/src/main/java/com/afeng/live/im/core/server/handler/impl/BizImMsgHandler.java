package com.afeng.live.im.core.server.handler.impl;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.SimplyHandler;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 业务消息的处理逻辑统一
 */
@Component
public class BizImMsgHandler implements SimplyHandler {
    private static final Logger logger = LoggerFactory.getLogger(BizImMsgHandler.class);

    @Resource
    private MQProducer mqProducer;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg msg) {
        System.out.println("this is biz msg");
        System.out.println( msg);
        //前期参数校验
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId==null){
            logger.error("attr error,msg is {}",msg);
            ctx.close();
            throw new RuntimeException("attr error");
        }

        byte[] body = msg.getBody();
        if (body == null || body.length == 0){
            logger.error("body is null,msg is {}",msg);
            return;
        }
        Message message = new Message();
        message.setBody(body);
        message.setTopic(ImCoreServerProviderTopicNames.AFENG_LIVE_IM_BIZ_MSG_TOPIC);
        try {
            SendResult sendResult = mqProducer.send(message);
            logger.info("[BizImMsgHandler] send msg success,msg is {},result is {}",msg,sendResult);
        } catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
            logger.error("[BizImMsgHandler] send msg error,msg is {}",msg);
            throw new RuntimeException(e);
        }
    }
}
