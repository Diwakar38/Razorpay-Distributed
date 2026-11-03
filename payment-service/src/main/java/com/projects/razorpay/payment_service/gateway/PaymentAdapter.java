package com.projects.razorpay.payment_service.gateway;


import com.projects.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.projects.razorpay.payment_service.gateway.dto.response.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {
    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
