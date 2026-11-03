package com.projects.razorpay.vault_service.dto.request;

import com.projects.razorpay.vault_service.validation.ExpiryYear;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Range;

import java.util.UUID;

public record TokenizeRequest(
        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "PAN length is invalid")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV length is invalid")
        String cvv,

        @NotNull(message = "Expiry month is required")
        @Range(min = 1, max = 12, message = "Expiry month should be between 1 and 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @ExpiryYear
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3, message = "CardHolderName should be at least 3 characters long")
        String cardHolderName
) {
}
