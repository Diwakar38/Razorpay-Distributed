package com.projects.razorpay.payment_service.outbox;

import com.projects.razorpay.common_lib.enums.EventAggregateType;
import com.projects.razorpay.payment_service.entity.OutboxEvent;
import com.projects.razorpay.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;

    public void publish(EventAggregateType aggregateType, UUID aggregateId,
                        String eventType, Map<String,Object> payload) {
        log.info("Publishing an event for aggregate type {} and aggregate id {}", aggregateType.name().toLowerCase(), aggregateId);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
