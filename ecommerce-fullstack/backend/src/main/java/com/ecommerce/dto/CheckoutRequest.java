package com.ecommerce.dto;

import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotNull(message = "addressId is required") Long addressId,
        @NotBlank(message = "paymentMethod is required") String paymentMethod,
        String upiId,
        String paymentReference,
        String paymentIntentId) {
    public CheckoutRequest(Long addressId, String paymentMethod) {
        this(addressId, paymentMethod, null, null, null);
    }
}
