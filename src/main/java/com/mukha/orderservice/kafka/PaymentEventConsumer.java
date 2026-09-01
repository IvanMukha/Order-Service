package com.mukha.orderservice.kafka;

import com.mukha.orderservice.kafka.event.PaymentCreatedEvent;
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "${order-service.kafka.topics.payment-created}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCreatedEvent(PaymentCreatedEvent event) {
        log.debug("Received {} event for orderId: {}, status: {}",
                event.eventType(), event.orderId(), event.status());

        OrderStatus newStatus = "SUCCESS".equals(event.status())
                ? OrderStatus.CONFIRMED
                : OrderStatus.CANCELLED;

        orderService.updateStatusByOrderId(event.orderId(), newStatus);
    }
}
