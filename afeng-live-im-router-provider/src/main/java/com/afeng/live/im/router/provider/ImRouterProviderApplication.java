package com.afeng.live.im.router.provider;

import com.afeng.live.im.router.provider.service.ImRouterService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableDubbo
public class ImRouterProviderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ImRouterProviderApplication.class, args);
    }

    @Resource
    private ImRouterService imRouterService;

    @Override
    public void run(String... args) throws Exception {

    }
}
