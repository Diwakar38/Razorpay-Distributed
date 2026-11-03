package com.projects.razorpay.payment_service.service;


import com.projects.razorpay.payment_service.dto.request.PaymentInitRequest;
import com.projects.razorpay.payment_service.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request, String idempotencyKey);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void reslveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription);
}
