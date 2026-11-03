package com.projects.razorpay.payment_service.gateway;

import com.projects.razorpay.common_lib.enums.PaymentMethod;
import com.projects.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.projects.razorpay.payment_service.gateway.dto.response.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiate(PaymentRequest request) {
        PaymentAdapter adapter = paymentAdapters.get(request.method());
        if(adapter == null) {
            throw new IllegalArgumentException("No payment adapter registered for method!!" + request.method());
        }
        return adapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod paymentMethod, UUID paymentId) {
        PaymentAdapter adapter = paymentAdapters.get(paymentMethod);
        if(adapter == null) {
            throw new IllegalArgumentException("No payment adapter registered for method!!" + paymentMethod);
        }
        return adapter.capture(paymentId);
    }
}
