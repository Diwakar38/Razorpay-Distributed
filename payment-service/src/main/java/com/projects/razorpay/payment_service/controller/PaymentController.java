package com.projects.razorpay.payment_service.controller;

import com.projects.razorpay.common_lib.context.MerchantContext;
import com.projects.razorpay.payment_service.dto.request.PaymentInitRequest;
import com.projects.razorpay.payment_service.dto.response.PaymentResponse;
import com.projects.razorpay.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentInitRequest request,
                                                    @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantContext.getMerchantId(), request, idempotencyKey));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}
