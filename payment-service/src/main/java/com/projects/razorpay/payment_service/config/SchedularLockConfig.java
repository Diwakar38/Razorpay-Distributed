package com.projects.razorpay.payment_service.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class SchedularLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory redis) {
        return new RedisLockProvider(redis);
    }
}
