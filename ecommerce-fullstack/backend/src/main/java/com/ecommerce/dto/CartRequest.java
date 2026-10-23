package com.ecommerce.dto;

import jakarta.validation.constraints.*;

public record CartRequest(
        @NotNull(message = "productId is required") Long productId,
        @NotNull(message = "quantity is required") @Min(value = 0, message = "quantity cannot be negative") Integer quantity) {}
