package com.afeng.live.user.provider.consumer;

import com.afeng.live.framework.mq.starter.config.RocketMQConsumerPropeties;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.user.constants.CacheAsyncDeleteCode;
import com.afeng.live.user.constants.UserProviderTopicNames;
import com.afeng.live.user.dto.UserCacheAsyncDeleteDto;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

@Component
public class RocketMQConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumer.class);

    @Resource
    private RocketMQConsumerPropeties rocketMQConsumerProperties;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;


    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + RocketMQConsumer.class.getSimpleName());
        mqPushConsumer.setConsumeMessageBatchMaxSize(1); // 每次消费1条
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET); // 从队列头开始消费
        mqPushConsumer.subscribe(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC, "");  // 订阅

        mqPushConsumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                UserCacheAsyncDeleteDto userCacheAsyncDeleteDto = JSON.parseObject(new String(msgs.get(0).getBody()), UserCacheAsyncDeleteDto.class);
                if (userCacheAsyncDeleteDto.getCode() == CacheAsyncDeleteCode.USER_INFO_DELETE.getCode()){
                    //用户信息删除
                    Long userId = JSON.parseObject(userCacheAsyncDeleteDto.getJson()).getLong("userId");
                    redisTemplate.delete(cacheKeyBuilder.buildUserInfoKey(userId));
                    LOGGER.info("用户信息删除，消息消费成功：{}",userId);
                }else if (userCacheAsyncDeleteDto.getCode() == CacheAsyncDeleteCode.USER_TAG_DELETE.getCode()){
                    //用户标签删除
                    Long userId = JSON.parseObject(userCacheAsyncDeleteDto.getJson()).getLong("userId");
                    redisTemplate.delete(cacheKeyBuilder.buildUserTagKey(userId));
                    LOGGER.info("用户标签删除，消息消费成功：{}",userId);
                }

                return CONSUME_SUCCESS;
            }
        });

        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }

}
