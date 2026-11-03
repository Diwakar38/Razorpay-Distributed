package com.projects.razorpay.merchant_service.service;


import com.projects.razorpay.merchant_service.dto.request.ApiKeyCreateRequest;
import com.projects.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.projects.razorpay.merchant_service.dto.response.ApiKeyRespose;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse createApiKey(UUID merchantId, ApiKeyCreateRequest request);

    List<ApiKeyRespose> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotateKey(UUID merchantId, UUID keyId);
}
