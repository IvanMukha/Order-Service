package com.mukha.orderservice.dto.response;

import java.math.BigDecimal;

public record ItemResponse(
        Long id,
        String name,
        BigDecimal price) {
}
