package com.projects.razorpay.payment_service.processor;


import com.projects.razorpay.common_lib.dto.PaymentProcessRequest;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;

public interface PaymentProcessor {

    PaymentProcessResponse charge(PaymentProcessRequest request);
}
