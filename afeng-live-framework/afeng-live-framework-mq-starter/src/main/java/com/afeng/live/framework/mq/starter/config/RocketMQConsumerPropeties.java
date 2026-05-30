package com.afeng.live.framework.mq.starter.config;

//import lombok.Data;
//import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "rocketmq.rmq.consumer")
@Configuration
public class RocketMQConsumerPropeties {
    private String nameSrv;
    private String groupName;


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
}
