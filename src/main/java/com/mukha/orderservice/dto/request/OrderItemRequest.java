package com.mukha.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "item id cannot be null")
        Long itemId,
        @NotNull(message = "quantity cannot be null")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity) {
}
