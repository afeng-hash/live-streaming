package com.afeng.live.living.provider.consumer;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.framework.mq.starter.config.RocketMQConsumerPropeties;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.im.core.server.interfaces.dto.ImOfflineDto;
import com.afeng.live.im.core.server.interfaces.dto.ImOnlineDto;
import com.afeng.live.living.provider.service.ILivingRoomService;
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
public class LivingRoomOfflineConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LivingRoomOfflineConsumer.class);

    @Resource
    private RocketMQConsumerPropeties rocketMQConsumerProperties;
    @Resource
    private ILivingRoomService iLivingRoomService;


    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + LivingRoomOfflineConsumer.class.getSimpleName());
        mqPushConsumer.setConsumeMessageBatchMaxSize(1); // 每次消费1条
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET); // 从队列头开始消费
        mqPushConsumer.subscribe(ImCoreServerProviderTopicNames.IM_OFFLINE_TOPIC, "");  // 订阅

        mqPushConsumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    LOGGER.info("[LivingRoomOfflineConsumer] receive msg,msg is {}", msg);
                    //清除redis中用户与roomid的关系
                    ImOfflineDto imOfflineDto = JSON.parseObject(new String(msg.getBody()), ImOfflineDto.class);
                    iLivingRoomService.userOfflineHandler(imOfflineDto);
                }

                return CONSUME_SUCCESS;
            }
        });

        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }

}
