package com.afeng.live.sms.provider;

import com.afeng.live.sms.provider.service.ISmsService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class SmsProviderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SmsProviderApplication.class, args);
    }


    @Resource
    private ISmsService iSmsService;

    @Override
    public void run(String... args) throws Exception {
//        iSmsService.sendLoginCode("17280269489");
    }
}
