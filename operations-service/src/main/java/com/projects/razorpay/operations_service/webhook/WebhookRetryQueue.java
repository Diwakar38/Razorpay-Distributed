package com.projects.razorpay.operations_service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryQueue {

    private final StringRedisTemplate redis;

    @Value("${app.webhook.delivery.redis-key:webhook-retry}")
    private String REDIS_KEY;

    public void enqueue(UUID webhookEventId, LocalDateTime retryAt) {
        long time = getTime(retryAt);
        redis.opsForZSet().add(REDIS_KEY, webhookEventId.toString(), time);
        log.info("Enqueued a webhook event with id: {}", webhookEventId);
    }

    public Set<UUID> pollDue(int limit) {
        long now = getTime(LocalDateTime.now());
        Set<ZSetOperations.TypedTuple<String>> due =
                redis.opsForZSet().rangeByScoreWithScores(REDIS_KEY, 0, now, 0, limit);

        if(due == null || due.isEmpty()) return Set.of();

        due.forEach(tuple ->
                            redis.opsForZSet().remove(REDIS_KEY, Objects.requireNonNull(tuple.getValue())));

        return due.stream()
                .map(tuple -> UUID.fromString(Objects.requireNonNull(tuple.getValue())))
                .collect(Collectors.toSet());
    }

    public void enqueueIfAbsent(UUID id, LocalDateTime nextRetryAt) {
        redis.opsForZSet().addIfAbsent(REDIS_KEY, id.toString(), getTime(nextRetryAt));
    }

    private static long getTime(LocalDateTime retryAt) {
        return retryAt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
