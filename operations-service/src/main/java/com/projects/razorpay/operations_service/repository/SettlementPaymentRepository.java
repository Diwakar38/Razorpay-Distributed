package com.projects.razorpay.operations_service.repository;

import com.projects.razorpay.operations_service.entity.SettlementPayment;
import com.projects.razorpay.operations_service.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
