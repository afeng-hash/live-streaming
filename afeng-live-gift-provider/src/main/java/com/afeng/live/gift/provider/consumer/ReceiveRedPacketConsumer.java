package com.afeng.live.gift.provider.consumer;

import com.afeng.live.common.interfaces.common.GiftProviderTopicNames;
import com.afeng.live.framework.mq.starter.config.RocketMQConsumerPropeties;
import com.afeng.live.gift.bo.SendRedPacketBO;
import com.afeng.live.gift.provider.service.IRedPacketConfigService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ReceiveRedPacketConsumer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(GiftConfigCacheConsumer.class);

    @Resource
    private RocketMQConsumerPropeties rocketMQConsumerPropeties;
    @Resource
    private IRedPacketConfigService iRedPacketConfigService;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        //老版本中会开启，新版本的mq不需要使用到
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerPropeties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerPropeties.getGroupName() + "_" + GiftConfigCacheConsumer.class.getSimpleName());
        //一次从broker中拉取10条消息到本地内存当中进行消费
        mqPushConsumer.setConsumeMessageBatchMaxSize(1);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //监听礼物缓存数据更新的行为
        mqPushConsumer.subscribe(GiftProviderTopicNames.RECEIVE_RED_PACKET, "");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            try {
                SendRedPacketBO sendRedPacketBO = JSON.parseObject(msgs.get(0).getBody(), SendRedPacketBO.class);
                iRedPacketConfigService.receiveRedPacketHandle(sendRedPacketBO.getReqDTO(),sendRedPacketBO.getPrice());
            }catch (Exception e){
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerPropeties.getNameSrv());
    }
}
