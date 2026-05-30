package com.afeng.live.framework.mq.starter.producer;

import com.afeng.live.framework.mq.starter.config.RocketMQProducerProperties;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * rocketMQ生产者的配置类
 */
@Configuration
public class RocketMQProducerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerConfig.class);

    @Autowired
    private RocketMQProducerProperties rocketMQProducerProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MQProducer mqProducer(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES
                , new ArrayBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(applicationName + ":rm-producer:"+ ThreadLocalRandom.current().nextInt(1000));
                return thread;
            }
        });

        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();

        try {
            defaultMQProducer.setProducerGroup(rocketMQProducerProperties.getGroupName());
            defaultMQProducer.setNamesrvAddr(rocketMQProducerProperties.getNameSrv());
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(rocketMQProducerProperties.getRetryTimes());
            defaultMQProducer.setSendMsgTimeout(rocketMQProducerProperties.getSendTimeout());
            defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK( true);
            //设置异步发送线程池
            defaultMQProducer.setAsyncSenderExecutor(threadPoolExecutor);
            defaultMQProducer.start();
            LOGGER.info("mq生产者启动成功,namesrv is {}", rocketMQProducerProperties.getNameSrv());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }

        return defaultMQProducer;
    }
}
