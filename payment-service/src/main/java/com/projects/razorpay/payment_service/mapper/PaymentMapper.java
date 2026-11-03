package com.projects.razorpay.payment_service.mapper;

import com.projects.razorpay.payment_service.dto.response.PaymentResponse;
import com.projects.razorpay.payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment payment);

    @Mapping(source = "order.id", target = "orderId")
    List<PaymentResponse> toResponseList(List<Payment> paymentList);
}
