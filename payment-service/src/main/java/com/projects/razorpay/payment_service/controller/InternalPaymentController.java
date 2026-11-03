package com.projects.razorpay.payment_service.controller;

import com.projects.razorpay.common_lib.dto.PaymentSettlementView;
import com.projects.razorpay.payment_service.api.PaymentLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalPaymentController {

    private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled-capture")
    List<PaymentSettlementView> findUnsettledCapturedPayments(@RequestParam UUID merchantId) {
        return paymentLookupService.findUnsettledCapturedPayments(merchantId);
    }

    @PostMapping("/mark-settled")
    void markSettled(@RequestBody List<UUID> paymentIds) {
        paymentLookupService.markSettled(paymentIds);
    }
}
