package com.projects.razorpay.payment_service.config;

import com.projects.razorpay.common_lib.enums.PaymentMethod;
import com.projects.razorpay.payment_service.gateway.PaymentAdapter;
import com.projects.razorpay.payment_service.gateway.adapter.CardPaymentAdapter;
import com.projects.razorpay.payment_service.gateway.adapter.NetBankingAdapter;
import com.projects.razorpay.payment_service.gateway.adapter.UPIAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final NetBankingAdapter netBankingAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;
    private final UPIAdapter upiAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NET_BANKING, netBankingAdapter,
                PaymentMethod.UPI, upiAdapter
        );
    }
}
