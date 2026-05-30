package com.afeng.live.im.core.server;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * netty启动类
 * 主要去接收客户端长连接功能
 * 后台的所有数据推送都是依靠这个服务与客户端的连接通道进行推送
 * 这个服务只负责做数据接收与推送
 */
@SpringBootApplication
@EnableDubbo
public class ImCoreServerApplication  {
    public static void main(String[] args) {
        SpringApplication.run(ImCoreServerApplication.class, args);
    }

}
