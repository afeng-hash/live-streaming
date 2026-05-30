package com.afeng.live.msg.provider.consumer;

import com.afeng.live.common.interfaces.common.ImCoreServerProviderTopicNames;
import com.afeng.live.framework.mq.starter.config.RocketMQConsumerPropeties;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.msg.provider.consumer.handler.MessageHandler;
import com.alibaba.fastjson.JSON;
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
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus.CONSUME_SUCCESS;


/**
 * 业务mq消息消费者
 */
@Component
public class ImMsgConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImMsgConsumer.class);

    @Resource
    private RocketMQConsumerPropeties rocketMQConsumerProperties;
    @Resource
    private MessageHandler singleMessageHandler;

    //需要记录每个用户连接的im服务器地址，然后根据im服务器的连接地址去做具体的机器的调用
    //基于mq广播思路去做，可能会有消息风暴发生，100台im机器，99的mq消息都是无效的
    //加入一个叫路由层的设计，router中转的设计，router就是一个dubbo的rpc层
    //A-> B im-core-server -> msg-provider(持久化，其他业务处理) -> im-core-server -> 通知到B
    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + ImMsgConsumer.class.getSimpleName());
        //一次从broker中拉取十条信息到本地内存当中进行消费
        mqPushConsumer.setConsumeMessageBatchMaxSize(10); // 每次消费10条
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET); // 从队列头开始消费
        mqPushConsumer.subscribe(ImCoreServerProviderTopicNames.AFENG_LIVE_IM_BIZ_MSG_TOPIC, "");  // 订阅

        mqPushConsumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    ImMsgBody imMsgBody = JSON.parseObject(new String(msg.getBody()), ImMsgBody.class);
                    // 处理业务逻辑
                    singleMessageHandler.onMsgReceive(imMsgBody);
                }
                return CONSUME_SUCCESS;
            }
        });

        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }

}
