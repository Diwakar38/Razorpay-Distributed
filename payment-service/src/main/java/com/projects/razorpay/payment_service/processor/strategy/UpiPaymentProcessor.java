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
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessResponse charge(PaymentProcessRequest request) {
        final String VPA_CODE_FAIL = "fail@oksbi";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("VPA").toString() : null;

        // Simulation
        if(VPA_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessResponse.Failure("UPI_REJECTED",
                                                      "Bank rejected the transaction registration");
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        String bankRef = "BANK_REF/" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessResponse.Pending(processorRef);
    }
}
