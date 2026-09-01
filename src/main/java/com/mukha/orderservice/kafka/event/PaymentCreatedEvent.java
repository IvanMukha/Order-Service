package com.mukha.orderservice.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreatedEvent(
        String eventType,
        Long orderId,
        Long userId,
        String status,
        BigDecimal paymentAmount,
        Instant timestamp) {
}
