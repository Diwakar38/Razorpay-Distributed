package com.projects.razorpay.vault_service.processor;

import com.projects.razorpay.common_lib.dto.PaymentProcessRequest;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import com.projects.razorpay.common_lib.util.RandomizerUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardPaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000069";

    @Bulkhead(name = "vault-card-processor", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<PaymentProcessResponse> charge(PaymentProcessRequest request) {
        String pan = request.pan();
        if(PAN_CARD_DECLINED.equals(pan))  {
            log.warn("Card Declined");
            return CompletableFuture.completedFuture(new PaymentProcessResponse.Failure("CARD_DECLINED", "Card declined by bank"));
        }
        if(PAN_CARD_EXPIRED.equals(pan))  {
            log.warn("Card Expired");
            return CompletableFuture.completedFuture(new PaymentProcessResponse.Failure("CARD_EXPIRED", "Card is expired"));
        }

        String processorRef = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return CompletableFuture.completedFuture(new PaymentProcessResponse.Pending(processorRef));
    }
}
