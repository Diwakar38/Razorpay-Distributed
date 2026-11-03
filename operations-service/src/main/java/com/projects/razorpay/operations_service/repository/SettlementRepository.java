package com.projects.razorpay.operations_service.repository;

import com.projects.razorpay.common_lib.enums.SettlementStatus;
import com.projects.razorpay.operations_service.entity.Settlement;
import com.projects.razorpay.operations_service.entity.SettlementPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByStatus(SettlementStatus settlementStatus);

    List<SettlementPayment> findBySettlement(Settlement settlement);
}
