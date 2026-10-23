package com.ecommerce.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "email is required") @Email(message = "invalid email") String email,
        @NotBlank(message = "password is required") @Size(min = 6, message = "password must be at least 6 characters") String password) {}
