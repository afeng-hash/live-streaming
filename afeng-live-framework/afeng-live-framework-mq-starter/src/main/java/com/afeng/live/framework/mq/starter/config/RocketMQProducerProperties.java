package com.afeng.live.framework.mq.starter.config;

//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 生产者的配置信息
 */
@ConfigurationProperties(prefix = "rocketmq.rmq.producer")
@Configuration
public class RocketMQProducerProperties {

    //rocketMQ的地址
    private String nameSrv;

    //生产者的组名
    private String groupName;

    //重发次数
    private int retryTimes;
    private int sendTimeout;

    public String getNameSrv() {
        return nameSrv;
    }

    public void setNameSrv(String nameSrv) {
        this.nameSrv = nameSrv;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }
}
