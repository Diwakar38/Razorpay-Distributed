package com.projects.razorpay.payment_service.gateway.dto.request;


import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
