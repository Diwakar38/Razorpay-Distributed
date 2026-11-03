package com.projects.razorpay.operations_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/webhook")
public class DummyMerchantWebhookController {

    @PostMapping("/success")
    public ResponseEntity<Void> handleWebhookSuccess(@RequestBody Map<String, Object> requestBody) {
        log.info("Successfully called Dummy Webhook Success");
        return ResponseEntity.noContent().build();
    }
}
