package com.projects.razorpay.operations_service.repository;

import com.projects.razorpay.common_lib.enums.WebHookEventStatus;
import com.projects.razorpay.operations_service.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    List<WebhookEvent> findByStatusAndNextRetryAtBefore(WebHookEventStatus webHookEventStatus, LocalDateTime now);
}
