package com.afeng.live.user.provider;

import com.afeng.live.user.constants.UserTagsEnum;
import com.afeng.live.user.dto.UserLoginDTO;
import com.afeng.live.user.provider.service.IUserPhoneService;
import com.afeng.live.user.provider.service.IUserTagService;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(UserProviderApplication.class, args);
    }

    @Autowired
    private IUserPhoneService userPhoneService;

    @Override
    public void run(String... args) throws Exception {

    }
}
