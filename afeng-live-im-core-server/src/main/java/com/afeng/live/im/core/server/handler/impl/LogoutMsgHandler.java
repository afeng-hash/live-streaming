package com.afeng.live.im.core.server.handler.impl;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.SimplyHandler;
import com.afeng.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import com.afeng.live.im.core.server.interfaces.dto.ImOfflineDto;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 登出消息的处理逻辑统一
 */
@Component
public class LogoutMsgHandler implements SimplyHandler {

    private static Logger logger = LoggerFactory.getLogger(LoginMsgHandler.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MQProducer mqProducer;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException {
        System.out.println("接收到消息【logout】：" + msg);
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null){
            logger.error("attr error,immsg is {}",msg);
            //有可能是错误的消息包导致，直接放弃连接
            ctx.close();
            throw new IllegalAccessException("attr is error");
        }
        logoutHandler(ctx, userId, appId, ImContextUtils.getRoomId(ctx));
    }


    /**
     * 登出的时候做缓存的清理和mq通知
     *
     * @param ctx
     * @param userId
     * @param appId
     */
    public void logoutHandler(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        logger.info("[LogoutMsgHandler] logout success,userId is {},appId is {}", userId, appId);
        //将im消息回写给客户端
        ImMsgBody imMsgBody = new ImMsgBody();
        imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
        imMsgBody.setData("true");
        imMsgBody.setUserId(userId);
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), JSON.toJSONString(imMsgBody));
        ctx.writeAndFlush(respMsg);

        //理想情况下，客户端断线的时候，会发送一个断线的消息包
        ChannelHandlerContextCache.remove(userId);
        //清理redis中的缓存
        stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBody.getAppId()+":"+userId);
        ImContextUtils.romeveUserId(ctx);
        ImContextUtils.romeveAppId(ctx);
        ImContextUtils.romeveRoomId(ctx);
        ctx.close();

        //发送mq通知
        sendLogoutMQ(ctx, userId, appId, roomId);
    }


    /**
     * 发送登出消息给MQ
     *
     * @param userId
     * @param appId
     * @param roomId
     */
    private void sendLogoutMQ(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        ImOfflineDto imOfflineDto = new ImOfflineDto();
        imOfflineDto.setUserId(userId);
        imOfflineDto.setAppId(appId);
        imOfflineDto.setRoomId(roomId);
        imOfflineDto.setLogoutTime(System.currentTimeMillis());
        Message message = new Message();
        message.setTopic(ImCoreServerProviderTopicNames.IM_OFFLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOfflineDto).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            logger.info("[LogoutMsgHandler] send logout msg to mq success,userId is {},appId is {},roomId is {}", userId, appId, roomId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
