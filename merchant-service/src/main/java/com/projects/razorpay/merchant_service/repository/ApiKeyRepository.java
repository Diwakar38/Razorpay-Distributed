package com.projects.razorpay.merchant_service.repository;

import com.projects.razorpay.merchant_service.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId); // _ means we're going one level deep in merchant

    Optional<ApiKey> findByKeyId(String keyId);
}
