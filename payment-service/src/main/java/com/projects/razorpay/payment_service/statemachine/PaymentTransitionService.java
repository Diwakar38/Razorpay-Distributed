package com.projects.razorpay.payment_service.statemachine;

import com.projects.razorpay.common_lib.context.MerchantContext;
import com.projects.razorpay.common_lib.enums.PaymentActor;
import com.projects.razorpay.common_lib.enums.PaymentEvent;
import com.projects.razorpay.common_lib.enums.PaymentStatus;
import com.projects.razorpay.payment_service.entity.Payment;
import com.projects.razorpay.payment_service.entity.PaymentTransactionLog;
import com.projects.razorpay.payment_service.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStatemachine paymentStatemachine;
    private final MerchantContext merchantContext;

    public PaymentStatus apply(Payment payment, PaymentEvent event) {
        PaymentStatus next = paymentStatemachine.transition(payment.getStatus(), event);
        payment.setStatus(next);
        PaymentTransactionLog log = PaymentTransactionLog.builder()
                .payment(payment)
                .from_status(payment.getStatus())
                .event_type(event)
                .to_status(next)
                .actor(getPaymentActor())
                .occuredAt(LocalDateTime.now())
                .build();

        paymentTransitionLogRepository.save(log);
        return next;
    }

    private PaymentActor getPaymentActor() {
        try {
            String keyId = merchantContext.getKeyId();
            UUID merchantId = merchantContext.getMerchantId();

            if(keyId != null && !keyId.isBlank()) {
                return PaymentActor.CUSTOMER;
            } else if (merchantId != null) {
                return PaymentActor.MERCHANT;
            }
        } catch (Exception ignored) {

        }
        return PaymentActor.SYSTEM;
    }
}
