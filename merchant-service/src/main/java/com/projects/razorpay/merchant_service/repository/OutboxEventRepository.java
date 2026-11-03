package com.projects.razorpay.merchant_service.repository;

import com.projects.razorpay.common_lib.enums.OutboxStatus;
import com.projects.razorpay.merchant_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
