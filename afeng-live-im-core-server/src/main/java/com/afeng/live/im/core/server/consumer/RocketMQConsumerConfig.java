package com.afeng.live.im.core.server.consumer;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.framework.mq.starter.config.RocketMQConsumerPropeties;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.im.core.server.service.IMsgAckCheckService;
import com.afeng.live.im.core.server.service.IRouterHandlerService;
import com.afeng.live.im.dto.ImMsgBody;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class RocketMQConsumerConfig implements InitializingBean {

    @Resource
    private RocketMQConsumerPropeties rocketMQConsumerProperties;
    @Resource
    private IMsgAckCheckService iMsgAckCheckService;
    @Resource
    private IRouterHandlerService iRouterHandlerService;


    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + RocketMQConsumerConfig.class.getSimpleName());
        mqPushConsumer.setConsumeMessageBatchMaxSize(1); // 每次消费1条
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET); // 从队列头开始消费
        mqPushConsumer.subscribe(ImCoreServerProviderTopicNames.AFENG_LIVE_IM_ACK_MSG_TOPIC, "");  // 订阅

        mqPushConsumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                String json = new String(msgs.get(0).getBody());
                ImMsgBody imMsgBody = JSON.parseObject(json, ImMsgBody.class);
                int retryTimes = iMsgAckCheckService.getMsgAckTimes(imMsgBody.getMsgId(), imMsgBody.getUserId(), imMsgBody.getAppId());
                if (retryTimes < 0) {
                    log.info("[RocketMQConsumerConfig] 消息处理了");
                    //说明消息被收到了
                    return CONSUME_SUCCESS;
                }
                //支持一次重发
                if (retryTimes < 2){
                    iMsgAckCheckService.recordMsgAck(imMsgBody,retryTimes+1);
                    iMsgAckCheckService.sendDelayMsg(imMsgBody);
                    log.info("[RocketMQConsumerConfig] 重新发送消息，msgId is {}",imMsgBody.getMsgId());
                    iRouterHandlerService.sendMsgToClient(imMsgBody);
                }else{
                    log.info("[RocketMQConsumerConfig] 消息重试次数超过限制，msgId is {}",imMsgBody.getMsgId());
                    iMsgAckCheckService.doMsgAck(imMsgBody);
                }
                return CONSUME_SUCCESS;
            }
        });

        mqPushConsumer.start();
        log.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }
}
