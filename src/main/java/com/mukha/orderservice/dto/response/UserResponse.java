package com.mukha.orderservice.dto.response;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email) {
}
