package com.projects.razorpay.merchant_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookConfigRequest(
        @NotBlank(message = "Webhook URL is required")
        @Size(max = 500)
        @Pattern(regexp = "^https?://.+", message = "Webhook URL must be valid http(s) URL")
        String targetUrl,

        @Size(max = 1000)
        String eventTypes
) {
}
