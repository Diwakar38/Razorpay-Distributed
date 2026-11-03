package com.projects.razorpay.payment_service.service.impl;

import com.projects.razorpay.common_lib.enums.EventAggregateType;
import com.projects.razorpay.common_lib.enums.OrderStatus;
import com.projects.razorpay.common_lib.enums.PaymentEvent;
import com.projects.razorpay.common_lib.enums.PaymentStatus;
import com.projects.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.projects.razorpay.payment_service.dto.request.PaymentInitRequest;
import com.projects.razorpay.payment_service.dto.response.PaymentResponse;
import com.projects.razorpay.payment_service.entity.OrderRecord;
import com.projects.razorpay.payment_service.entity.Payment;
import com.projects.razorpay.payment_service.gateway.PaymentGatewayRouter;
import com.projects.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.projects.razorpay.payment_service.gateway.dto.response.PaymentResult;
import com.projects.razorpay.payment_service.mapper.PaymentMapper;
import com.projects.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.projects.razorpay.payment_service.repository.OrderRepository;
import com.projects.razorpay.payment_service.repository.PaymentRepository;
import com.projects.razorpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.projects.razorpay.payment_service.service.PaymentService;
import com.projects.razorpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Override
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request, String idempotencyKey) {

        if(idempotencyKey != null) {
            var existing = paymentAuthorizationRecorder.findExistingAttempt(merchantId, idempotencyKey);
            if(existing.isPresent()) {
                log.info("Idempotency replay for paymentId: {}", existing.get().id());
                return existing.get();
            }
        }

        Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId, request, idempotencyKey);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                request.orderId(),
                merchantId,
                payment.getAmount(),
                request.method(),
                request.methodDetails());

        PaymentResult result;
        try {
            result = paymentGatewayRouter.initiate(paymentRequest);
        } catch (Exception e) {
            return paymentAuthorizationRecorder.compensateAuthorizationFailure(
                    payment.getId(), "PAYMENT_GATEWAY_ROUTER_UNREACHABLE" , e.getMessage());
        }

        return paymentAuthorizationRecorder.applyGatewayResults(payment.getId(), result);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
//        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getPaymentMethod(), paymentId);

        switch (paymentResult) {
            case PaymentResult.Success success -> {
                paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                log.info("Payment Captured, paymentId: {}", paymentId);
            }
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAILURE);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
                log.warn("Payment Failed, paymentId: {}", paymentId);
            }
            case PaymentResult.Pending pending -> {
            }
        }

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENTS, payment.getId(), "PAYMENT_STATUS_CHANGED",
                               Map.of("orderId", payment.getOrder().getId().toString(),
                                      "paymentId", payment.getId().toString(),
                                      "merchantId", payment.getMerchantId().toString(),
                                      "paymentStatus", payment.getStatus().name(),
                                      "amountUnits", payment.getAmount().getAmountUnits(),
                                      "amountCurrency", payment.getAmount().getCurrency(),
                                      "paymentMethod", payment.getPaymentMethod().name()
                               ));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void reslveAuthorization(UUID paymentId, boolean approve,
                                    String bankRef, String errorCode, String errorDescription) {
//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if(payment.getStatus() != PaymentStatus.AUTHORIZING) {
            log.warn("Payment is not in authorizing state, paymentId: {}, status: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();

        if(approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());
            // Auto Capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getPaymentMethod(), paymentId);

            switch (paymentResult) {
                case PaymentResult.Success success -> {
                    paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                    payment.setCapturedAt(LocalDateTime.now());
                    orderRecord.setOrderStatus(OrderStatus.PAID);
                }
                case PaymentResult.Pending pending -> {
                    paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                }
                case PaymentResult.Failure failure -> {
                    paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                    payment.setErrorCode(failure.errorCode());
                    payment.setErrorDescription(failure.errorDescription());
                }
            }
        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
        eventPublisher.publish(EventAggregateType.PAYMENTS, payment.getId(), "PAYMENT_STATUS_CHANGED",
                               Map.of("orderId", payment.getOrder().getId().toString(),
                                      "paymentId", payment.getId().toString(),
                                      "merchantId", payment.getMerchantId().toString(),
                                      "paymentStatus", payment.getStatus().name(),
                                      "amountUnits", payment.getAmount().getAmountUnits(),
                                      "amountCurrency", payment.getAmount().getCurrency(),
                                      "paymentMethod", payment.getPaymentMethod().name()
                               ));
    }
}
