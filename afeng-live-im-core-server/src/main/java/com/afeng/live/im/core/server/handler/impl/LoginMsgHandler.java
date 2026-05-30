package com.afeng.live.im.core.server.handler.impl;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.constants.ImConstants;
import com.afeng.live.im.core.server.ImCoreServerApplication;
import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.ImContextAttr;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.SimplyHandler;
import com.afeng.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import com.afeng.live.im.core.server.interfaces.dto.ImOnlineDto;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.afeng.live.im.interfaces.ImTokenRpc;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 登录消息的处理逻辑统一
 */
@Component
public class LoginMsgHandler implements SimplyHandler {

    private static Logger logger = LoggerFactory.getLogger(LoginMsgHandler.class);

    @DubboReference
    private ImTokenRpc imTokenRpc;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MQProducer mqProducer;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException {
        //防重复请求
        if (ImContextUtils.getUserId(ctx) != null){
            return;
        }
        System.out.println("接收到消息【login】：" + msg);
        byte[] body = msg.getBody();
        if (body == null || body.length == 0){
            ctx.close();
            logger.error("body error,immsg is {}", msg);
            throw new IllegalAccessException("body error");
        }

        ImMsgBody imMsgBody = JSON.parseObject(new String(body), ImMsgBody.class);
        String token = imMsgBody.getToken();
        if (StringUtils.isEmpty(token)){
            ctx.close();
            logger.error("token error,immsg is {}", msg);
            throw new IllegalAccessException("token error");
        }

        Long userId = imTokenRpc.getUserIdByToken(token);
        //token校验成功，而且传递过来的userId是同一个，则允许建立连接
        if (userId != null && userId.equals(imMsgBody.getUserId())){
            loginSuccessHandler(ctx,userId,imMsgBody.getAppId(),null);
        }else{
            ctx.close();
            logger.error("token check error,token:{},immsg is {}",token,msg);
            throw new IllegalAccessException("token check error");
        }

    }



    /**
     * 如果用户登录成功则处理相关记录
     *
     * @param ctx
     * @param userId
     * @param appId
     */
    public void loginSuccessHandler(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        //按照userId保存好相关的channel对象信息
        //按照userId保存好相关的channel对象信息
        ChannelHandlerContextCache.put(userId,ctx);
        //给通道绑定上userId
        ImContextUtils.setUserId(ctx,userId);
        //给通道绑定上appId
        ImContextUtils.setAppId(ctx,appId);
        if (roomId != null) {
            ImContextUtils.setRoomId(ctx, roomId);
        }
        //将im消息回写给客户端
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(appId);
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), com.alibaba.fastjson.JSON.toJSONString(respBody));

        //在redis中缓存userId与机器地址关系
        stringRedisTemplate.opsForValue().set(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId,
                ChannelHandlerContextCache.getServerIpAddress() + "%" + userId,
                ImConstants.DEFAULT_HEART_BEAT_GAP * 2, TimeUnit.SECONDS);

        logger.info("[LoginMsgHandler] login success,userId is {},appId is {}", userId, appId);
        ctx.writeAndFlush(respMsg);

        //发送登录消息给MQ
        sendLoginMQ(userId, appId, roomId);
    }


    /**
     * 发送登录消息给MQ
     *
     * @param userId
     * @param appId
     * @param roomId
     */
    private void sendLoginMQ(Long userId, Integer appId, Integer roomId) {
        ImOnlineDto imOnlineDto = new ImOnlineDto();
        imOnlineDto.setUserId(userId);
        imOnlineDto.setAppId(appId);
        imOnlineDto.setRoomId(roomId);
        imOnlineDto.setLoginTime(System.currentTimeMillis());
        Message message = new Message();
        message.setTopic(ImCoreServerProviderTopicNames.IM_ONLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOnlineDto).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            logger.info("[LoginMsgHandler] send login msg to mq success,userId is {},appId is {},roomId is {}", userId, appId, roomId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
