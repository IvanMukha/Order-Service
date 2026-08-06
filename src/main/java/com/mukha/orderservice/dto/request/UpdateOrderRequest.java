package com.mukha.orderservice.dto.request;

import com.mukha.orderservice.model.status.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequest(

        @NotNull(message = "status cannot be null")
        OrderStatus status,

        @Valid
        @NotEmpty(message = "order item list cannot be empty")
        List<OrderItemRequest> orderItems) {
}
