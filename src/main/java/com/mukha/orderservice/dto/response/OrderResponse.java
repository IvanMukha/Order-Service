package com.mukha.orderservice.dto.response;

import com.mukha.orderservice.model.status.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponse> orderItems,
        UserResponse user) {

}
