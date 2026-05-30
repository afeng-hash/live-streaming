package com.afeng.live.im.core.server.handler;

import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.impl.ImHandlerFactoryImpl;
import com.afeng.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * im消息统一handler入口
 */
@Slf4j
@Component
public class ImServerCoreHandler extends SimpleChannelInboundHandler {

    @Resource
    private ImHandlerFactory imHandlerFactory;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ImMsg)){
            throw new Exception("error msg,msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        //登录消息包，登录token认证，channel和userId关联
        //登出消息包，正常断开im连接的时候发送
        //业务消息包，最常用的消息类型，例如我们的im发送数据，或者接收数据的时候会用到
        //心跳消息包，定时会给im发送，汇报功能
        imHandlerFactory.doMsgHandler(ctx,imMsg);
    }

    /**
     * 正常断线或者意外断线，都会触发到这里
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Integer appId = ImContextUtils.getAppId(ctx);
        Long userId = ImContextUtils.getUserId(ctx);
        log.info("[ImServerCoreHandler] 断线处理：userId:{}，appId:{}",userId,appId);
        if (userId!=null && appId!=null){
            ChannelHandlerContextCache.remove(userId);
            //移除缓存中用户与机器关系
            stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":"+userId);
            ImContextUtils.romeveUserId(ctx);
            ImContextUtils.romeveAppId(ctx);
        }
    }
}
