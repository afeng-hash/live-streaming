package com.afeng.live.im.core.server.service.impl;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.framework.redis.starter.keys.ImCoreServerProviderCacheKeyBuilder;
import com.afeng.live.im.core.server.service.IMsgAckCheckService;
import com.afeng.live.im.dto.ImMsgBody;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class IMsgAckCheckServiceImpl implements IMsgAckCheckService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    /**
     * 客户端发送ack包给服务器后，调用进行ack记录的移除
     * @param imMsgBody
     */
    @Override
    public void doMsgAck(ImMsgBody imMsgBody) {
        log.info("[IMsgAckCheckServiceImpl] doMsgAck,msg is {}",imMsgBody);
        redisTemplate.opsForHash().delete(cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(),imMsgBody.getAppId()),imMsgBody.getMsgId());
    }

    /**
     * 发送延迟消息，用于进行消息重试功能
     * @param imMsgBody
     */
    @Override
    public void recordMsgAck(ImMsgBody imMsgBody, int times) {
        redisTemplate.opsForHash().put(cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(),imMsgBody.getAppId()),imMsgBody.getMsgId(),times);
    }

    @Override
    public void sendDelayMsg(ImMsgBody imMsgBody) {
        String json = JSON.toJSONString(imMsgBody);
        Message message = new Message();
        message.setBody(json.getBytes());
        message.setTopic(ImCoreServerProviderTopicNames.AFENG_LIVE_IM_ACK_MSG_TOPIC);
        //等级1-》1s，等级2-》5s
        message.setDelayTimeLevel(2);
        try {
            mqProducer.send(message);
            log.info("[IMsgAckCheckServiceImpl] sendDelayMsg success,msg is {}",imMsgBody);
        } catch (Exception e) {
            log.error("[IMsgAckCheckServiceImpl] sendDelayMsg error,msg is {}",imMsgBody);
        }
    }

    @Override
    public int getMsgAckTimes(String msgId, long userId, int appId) {
        Object value = redisTemplate.opsForHash().get(cacheKeyBuilder.buildImAckMapKey(userId, appId), msgId);
        if (value == null){
            return -1;
        }
        return Integer.parseInt(value.toString());
    }
}
