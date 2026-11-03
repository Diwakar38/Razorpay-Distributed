package com.projects.razorpay.common_lib.dto;


import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod method,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
) {

    public static PaymentProcessRequest card(UUID paymentId, String pan, String expiry, Money amount, Map<String,Object> details) {
        return new PaymentProcessRequest(UUID.randomUUID(), paymentId, PaymentMethod.CARD, amount, pan, expiry, details);
    }

    public static PaymentProcessRequest nonCard(UUID paymentId, PaymentMethod paymentMethod, Money amount, Map<String,Object> details) {
        return new PaymentProcessRequest(UUID.randomUUID(), paymentId, paymentMethod, amount, null, null, details);
    }
}
