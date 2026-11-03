package com.projects.razorpay.merchant_service.dto.request;


import com.projects.razorpay.common_lib.enums.Environment;

public record ApiKeyCreateRequest(
        Environment environment

) {
}
