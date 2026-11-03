package com.projects.razorpay.payment_service.clients;

import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import com.projects.razorpay.common_lib.dto.VaultChargeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", path = "/internal/vault")
public interface VaultServiceClient {

    @PostMapping("/charge")
    PaymentProcessResponse charge(@RequestBody VaultChargeRequest request);

}
