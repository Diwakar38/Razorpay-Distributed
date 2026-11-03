package com.projects.razorpay.operations_service.webhook;

import com.projects.razorpay.common_lib.enums.WebHookEventStatus;
import com.projects.razorpay.operations_service.entity.WebhookEvent;
import com.projects.razorpay.operations_service.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryScheduler {
    private final WebhookRetryQueue retryQueue;
    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryExecutor deliveryExecutor;

    private ExecutorService virtualThreadExecutor;

    @PostConstruct
    void init() {
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    void shutdown() {
        virtualThreadExecutor.shutdown();
    }

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize;

    @Scheduled(fixedRate = 1000) // After execution of each schedule, it sleeps for 1000 ms
    // Let's say it takes x seconds then it'll run after x+1000 s
    @SchedulerLock(name = "operations-service-webhook-delivery-poll-and-deliver", lockAtMostFor = "10s", lockAtLeastFor = "1s")
    public void pollAndDeliver() {
        Set<UUID> due = retryQueue.pollDue(batchSize);

        if(due.isEmpty()) return;

        for(UUID webhookEventId : due) {
            virtualThreadExecutor.submit(() -> {
                deliveryExecutor.deliver(webhookEventId);
            });
        }
    }

    @Scheduled(fixedRate = 10000)
    @SchedulerLock(name = "operations-service-webhook-delivery-reconcile-from-db", lockAtMostFor = "10s", lockAtLeastFor = "1s")
    public void reconcileFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        List<WebhookEvent> due = eventRepository.
                findByStatusAndNextRetryAtBefore(WebHookEventStatus.PENDING, now);

        for(WebhookEvent event : due) {
            retryQueue.enqueueIfAbsent(event.getId(), event.getNextRetryAt());
        }
    }
}
