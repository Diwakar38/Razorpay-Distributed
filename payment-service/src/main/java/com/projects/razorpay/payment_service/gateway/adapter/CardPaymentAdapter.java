package com.projects.razorpay.payment_service.gateway.adapter;

import com.projects.razorpay.common_lib.dto.VaultChargeRequest;
import com.projects.razorpay.payment_service.clients.VaultServiceClient;
import com.projects.razorpay.payment_service.gateway.PaymentAdapter;
import com.projects.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.projects.razorpay.payment_service.gateway.dto.response.PaymentResult;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token = request.methodDetails().get("token").toString();
        PaymentProcessResponse response = vaultServiceClient.charge(
                new VaultChargeRequest(request.paymentId(),
                                       token,
                                       request.amount(),
                                       request.methodDetails())
        );
        return switch (response) {
            case PaymentProcessResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");

    }
}
