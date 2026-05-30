package com.afeng.live.web.starter.config;

import com.afeng.live.web.starter.filter.AfengUserInfoInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public AfengUserInfoInterceptor afengUserInfoInterceptor() {
        return new AfengUserInfoInterceptor();
    }

    @Bean
    public AfengUserInfoInterceptor requestLimitInterceptor(){
        return new AfengUserInfoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(afengUserInfoInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
        registry.addInterceptor(requestLimitInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
    }
}
