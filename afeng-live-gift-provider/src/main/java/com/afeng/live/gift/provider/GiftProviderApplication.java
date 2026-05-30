package com.afeng.live.gift.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class GiftProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftProviderApplication.class, args);
    }
}
