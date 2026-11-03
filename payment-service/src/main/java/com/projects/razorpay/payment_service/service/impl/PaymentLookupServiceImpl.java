package com.projects.razorpay.payment_service.service.impl;

import com.projects.razorpay.common_lib.dto.PaymentSettlementView;
import com.projects.razorpay.common_lib.enums.PaymentStatus;
import com.projects.razorpay.payment_service.api.PaymentLookupService;
import com.projects.razorpay.payment_service.entity.Payment;
import com.projects.razorpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentLookupServiceImpl implements PaymentLookupService {
    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        List<Payment> payments = paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
        return payments.stream()
                .map(p -> new PaymentSettlementView(
                        p.getId(),
                        p.getAmount().getAmountUnits(),
                        0, // TODO: replace with actual refund values from RefundRepository
                        p.getAmount().getCurrency()
                )).toList();
    }

    @Override
    public void markSettled(List<UUID> paymentIds) {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findAllById(paymentIds);
        payments.forEach(p -> {
                    p.setStatus(PaymentStatus.SETTLED);
                    p.setSettledAt(now);
                });
        paymentRepository.saveAll(payments);
    }
}
