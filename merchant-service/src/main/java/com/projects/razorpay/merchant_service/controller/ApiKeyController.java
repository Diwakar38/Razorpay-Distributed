package com.projects.razorpay.merchant_service.controller;

import com.projects.razorpay.common_lib.context.MerchantContext;
import com.projects.razorpay.merchant_service.dto.request.ApiKeyCreateRequest;
import com.projects.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.projects.razorpay.merchant_service.dto.response.ApiKeyRespose;
import com.projects.razorpay.merchant_service.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@Valid @RequestBody ApiKeyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.createApiKey(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyRespose>> listByMerchant() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .body(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID keyId) {
        apiKeyService.revoke(merchantContext.getMerchantId(),keyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotateKey(@PathVariable UUID keyId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(apiKeyService.rotateKey(merchantContext.getMerchantId(), keyId));
    }
}
