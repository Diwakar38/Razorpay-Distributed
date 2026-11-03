package com.projects.razorpay.vault_service.service;


import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.vault_service.dto.request.TokenizeRequest;
import com.projects.razorpay.vault_service.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
