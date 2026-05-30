package com.afeng.live.framework.datasource.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@ConditionalOnClass(DataSource.class)
public class ShardingJdbcDatasourceAutoInit {

    @Bean
//    @ConditionalOnBean(DataSource.class)
    public ApplicationRunner runner(DataSource dataSource){
        return args -> {
            System.out.println("数据源初始化完成");
            Connection connection = dataSource.getConnection();
        };
    }
}
