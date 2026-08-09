package com.mukha.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank(message = "Item name cannot be empty")
        String name,
        @NotNull(message = "Item price cannot be null")
        @Positive(message = "Item price must be positive")
        BigDecimal price) {
}
