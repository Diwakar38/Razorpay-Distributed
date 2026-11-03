package com.projects.razorpay.payment_service.service;


import com.projects.razorpay.payment_service.dto.request.CreateOrderRequest;
import com.projects.razorpay.payment_service.dto.response.OrderResponse;
import com.projects.razorpay.payment_service.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(CreateOrderRequest request, UUID merchantId);

    OrderResponse getById(UUID merchantId, UUID orderId);

    OrderResponse cancel(UUID merchantId, UUID orderId);

    List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}
