package com.projects.razorpay.common_lib.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiter{

    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREFIX = "ratelimit:fixed";

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {
        String redisKey = PREFIX + key;

        Long count = stringRedisTemplate.opsForValue().increment(redisKey);

        if(count == null) return RateLimitResult.allowed(maxRequestAllowed);
        if(count == 1) {
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(60));
        }
        if(count > maxRequestAllowed) {
            Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            int retryAfter = (ttl != null && ttl > 0) ? ttl.intValue() : (int)windowSeconds;
            return RateLimitResult.denied(retryAfter);
        }

        return RateLimitResult.allowed((int) (maxRequestAllowed - count));
    }
}
