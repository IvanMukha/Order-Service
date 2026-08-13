package com.mukha.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @Valid
        @NotEmpty(message = "order item list cannot be empty")
        List<OrderItemRequest> orderItems) {
}
