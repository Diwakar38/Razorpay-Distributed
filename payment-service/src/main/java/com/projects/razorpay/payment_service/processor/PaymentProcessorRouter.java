package com.projects.razorpay.payment_service.processor;

import com.projects.razorpay.common_lib.enums.PaymentMethod;
import com.projects.razorpay.common_lib.dto.PaymentProcessRequest;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessResponse charge(PaymentProcessRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessors.get(request.method());
        if(paymentProcessor == null) {
            throw new IllegalArgumentException("No payment processor registered for method!!" + request.method());
        }

        return paymentProcessor.charge(request);
    }
}
