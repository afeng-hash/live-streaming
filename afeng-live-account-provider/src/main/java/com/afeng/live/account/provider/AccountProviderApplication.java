package com.afeng.live.account.provider;

import com.afeng.live.account.provider.service.impl.AccountTokenServiceImpl;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class AccountProviderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(AccountProviderApplication.class, args);
    }

    @Autowired
    private AccountTokenServiceImpl accountTokenService;

    @Override
    public void run(String... args) throws Exception {
    }
}
