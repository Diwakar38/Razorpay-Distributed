package com.projects.razorpay.payment_service.repository;

import com.projects.razorpay.payment_service.entity.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTransitionLogRepository extends JpaRepository<PaymentTransactionLog, UUID> {
}
