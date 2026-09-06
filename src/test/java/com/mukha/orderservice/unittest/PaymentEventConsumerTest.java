package com.mukha.orderservice.unittest;

import com.mukha.orderservice.kafka.PaymentEventConsumer;
import com.mukha.orderservice.kafka.event.PaymentCreatedEvent;
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private static final Long ORDER_ID = 10L;
    private static final Long USER_ID = 1L;

    @Test
    void handlePaymentCreatedEvent_shouldUpdateOrderStatusToConfirmed_whenPaymentStatusIsSuccess() {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                "CREATE_PAYMENT", ORDER_ID, USER_ID, "SUCCESS", BigDecimal.valueOf(150), Instant.now());

        paymentEventConsumer.handlePaymentCreatedEvent(event);

        verify(orderService).updateStatusByOrderId(ORDER_ID, OrderStatus.CONFIRMED);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void handlePaymentCreatedEvent_shouldUpdateOrderStatusToCancelled_whenPaymentStatusIsFailed() {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                "CREATE_PAYMENT", ORDER_ID, USER_ID, "FAILED", BigDecimal.valueOf(150), Instant.now());

        paymentEventConsumer.handlePaymentCreatedEvent(event);

        verify(orderService).updateStatusByOrderId(ORDER_ID, OrderStatus.CANCELLED);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void handlePaymentCreatedEvent_shouldUpdateOrderStatusToCancelled_whenPaymentStatusIsUnknown() {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                "CREATE_PAYMENT", ORDER_ID, USER_ID, "PENDING", BigDecimal.valueOf(150), Instant.now());

        paymentEventConsumer.handlePaymentCreatedEvent(event);

        verify(orderService).updateStatusByOrderId(ORDER_ID, OrderStatus.CANCELLED);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void handlePaymentCreatedEvent_shouldPassCorrectOrderIdToOrderService() {
        Long specificOrderId = 999L;
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                "CREATE_PAYMENT", specificOrderId, USER_ID, "SUCCESS", BigDecimal.TEN, Instant.now());

        paymentEventConsumer.handlePaymentCreatedEvent(event);

        verify(orderService).updateStatusByOrderId(specificOrderId, OrderStatus.CONFIRMED);
    }
}