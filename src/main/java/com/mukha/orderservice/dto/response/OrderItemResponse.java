package com.mukha.orderservice.dto.response;

public record OrderItemResponse(
    Long id,
    Long itemId,
    Integer quantity){
}
