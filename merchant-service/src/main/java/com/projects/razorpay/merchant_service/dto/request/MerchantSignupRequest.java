package com.projects.razorpay.merchant_service.dto.request;

import com.projects.razorpay.common_lib.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(

        @NotNull(message = "Name is required")
        @Size(max = 80, message = "Name should not be more than 80 characters")
        String name,

        @Email
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
        String password,

        @Size(max = 50,  message = "Business name should not be more than 50 characters")
        String businessName,

        BusinessType businessType

) {
}
