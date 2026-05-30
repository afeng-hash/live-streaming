package com.afeng.live.im.provider;

import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.interfaces.ImTokenRpc;
import com.afeng.live.im.provider.service.ImOnlineService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 负责去监控，比如判断用户是否在线
 */
@EnableDubbo
@SpringBootApplication
public class ImProviderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ImProviderApplication.class, args);
    }

    @Resource
    private ImOnlineService imOnlineService;

    @Override
    public void run(String... args) throws Exception {
//        System.out.println(imOnlineService.isOnline(10001L, 1));
//        System.out.println(imOnlineService.isOnline(10002L, 1));
    }
}
