package com.projects.razorpay.payment_service.processor.strategy;

import com.projects.razorpay.common_lib.util.RandomizerUtil;
import com.projects.razorpay.payment_service.processor.PaymentProcessor;
import com.projects.razorpay.common_lib.dto.PaymentProcessRequest;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessResponse charge(PaymentProcessRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("BANK").toString() : null;

        // Simulation
        if(BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessResponse.Failure("BANK_REJECTED",
                                                      "Bank rejected the transaction registration");
        }

        String processorRef = "NBK_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        String redirectRef = "http://REDIRECT_BANK.com/" + processorRef;

        return new PaymentProcessResponse.Pending(processorRef);
    }
}
