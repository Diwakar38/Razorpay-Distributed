package com.projects.razorpay.payment_service.service.impl;

import com.projects.razorpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.projects.razorpay.common_lib.enums.EventAggregateType;
import com.projects.razorpay.common_lib.enums.OrderStatus;
import com.projects.razorpay.common_lib.exceptions.BusinessRuleViolationException;
import com.projects.razorpay.common_lib.exceptions.DuplicateResourceException;
import com.projects.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.projects.razorpay.payment_service.clients.CustomerServiceClient;
import com.projects.razorpay.payment_service.dto.request.CreateOrderRequest;
import com.projects.razorpay.payment_service.dto.response.OrderResponse;
import com.projects.razorpay.payment_service.dto.response.PaymentResponse;
import com.projects.razorpay.payment_service.entity.OrderRecord;
import com.projects.razorpay.payment_service.entity.Payment;
import com.projects.razorpay.payment_service.mapper.OrderMapper;
import com.projects.razorpay.payment_service.mapper.PaymentMapper;
import com.projects.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.projects.razorpay.payment_service.repository.OrderRepository;
import com.projects.razorpay.payment_service.repository.PaymentRepository;
import com.projects.razorpay.payment_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerServiceClient customerServiceClient;
    private final OutboxEventPublisher eventPublisher;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    public OrderResponse create(CreateOrderRequest request, UUID merchantId) {
        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + request.receipt());
        }

        UUID customerId = null;
        if(request.customer() != null) {
            FindOrCreateCustomerRequest req = new FindOrCreateCustomerRequest(
                    merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()
            );
            customerId = customerServiceClient.findOrCreate(req);
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .customerId(customerId)
                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDERS, order.getId(), "ORDER_CREATED",
                               Map.of("orderId", order.getId().toString(),
                                      "merchantId", merchantId.toString(),
                                      "orderStatus", order.getOrderStatus().name(),
                                      "amountUnits", order.getAmount().getAmountUnits(),
                                      "amountCurrency", order.getAmount().getCurrency()
                                      ));

//        return new OrderResponse(
//                order.getId(),
//                order.getMerchantId(),
//                order.getReceipt(),
//                order.getAmount(),
//                order.getOrderStatus(),
//                order.getAttempts(),
//                order.getNotes(),
//                order.getExpiresAt(),
//                null); // TODO: Add after auditing

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order_id",orderId));
//        return new OrderResponse(
//                orderId,
//                merchantId,
//                order.getReceipt(),
//                order.getAmount(),
//                order.getOrderStatus(),
//                order.getAttempts(),
//                order.getNotes(),
//                order.getExpiresAt(),
//                null);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order_id", orderId));

        if(order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELLED)
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL",
                                                     "Cannot cancel order with status: " + order.getOrderStatus().name());

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDERS, order.getId(), "ORDER_CANCELLED",
                               Map.of("orderId", order.getId().toString(),
                                      "merchantId", merchantId.toString(),
                                      "orderStatus", order.getOrderStatus().name(),
                                      "amountUnits", order.getAmount().getAmountUnits(),
                                      "amountCurrency", order.getAmount().getCurrency()
                               ));
        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order_id",orderId));
        List<Payment> payments = paymentRepository.findByOrder_Id(orderId);

//        return payments.stream().map(
//                payment -> paymentMapper.toCreateResponse(payment)
//        ).collect(Collectors.toList());

        return paymentMapper.toResponseList(payments);
    }
}
