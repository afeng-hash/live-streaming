package com.afeng.live.im.core.server.handler.impl;

import com.afeng.live.framework.redis.starter.keys.ImCoreServerProviderCacheKeyBuilder;
import com.afeng.live.im.constants.ImConstants;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.handler.SimplyHandler;
import com.afeng.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import com.afeng.live.im.core.server.utils.ImContextUtils;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 心跳消息的处理逻辑统一
 */
@Component
public class HeartBeatImMsgHandler implements SimplyHandler {

    private static Logger logger = LoggerFactory.getLogger(HeartBeatImMsgHandler.class);

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg msg) throws IllegalAccessException {
//        System.out.println("this is heart");
        //心跳包的基本校验
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId==null){
            logger.error("attr error，immsg is {}",msg);
            ctx.close();
            throw new IllegalAccessException("attr is error");
        }

        //心跳包records记录，redis存储心跳记录
        String key = cacheKeyBuilder.buildImLoginTokenKey(userId, appId);
        redisTemplate.opsForZSet().add(key,userId,System.currentTimeMillis());
        this.removeExpireRecord(key);
        redisTemplate.expire(key,5, TimeUnit.MINUTES);
        //给redis中用户与ip的关系进行续期
        stringRedisTemplate.expire(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId,
                ImConstants.DEFAULT_HEART_BEAT_GAP * 3,
                TimeUnit.SECONDS);

        ImMsgBody resBody = new ImMsgBody();
        resBody.setAppId(appId);
        resBody.setUserId(userId);
        resBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), JSON.toJSONString(resBody));
        ctx.writeAndFlush(respMsg);
    }

    /**
     * 清理掉过期不在线的用户留下的心跳记录（在俩次心跳包的发送间隔中，如果没有重新更新score值，就会导致被删除）
     * @param redisKey
     */
    private void removeExpireRecord(String redisKey){
        redisTemplate.opsForZSet().removeRangeByScore(redisKey,0,System.currentTimeMillis()-1000* ImConstants.DEFAULT_HEART_BEAT_GAP*2);
    }
}
