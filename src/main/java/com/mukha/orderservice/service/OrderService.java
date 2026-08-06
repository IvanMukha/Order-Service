package com.mukha.orderservice.service;

import com.mukha.orderservice.dto.request.CreateOrderRequest;
import com.mukha.orderservice.dto.request.UpdateOrderRequest;
import com.mukha.orderservice.dto.response.OrderResponse;
import com.mukha.orderservice.model.status.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest createOrderRequest, String userId);

    OrderResponse getById(Long id);

    Page<OrderResponse> getAll(Long userId,LocalDateTime startDate, LocalDateTime endDate, List<OrderStatus> orderStatuses, Pageable pageable);

    OrderResponse updateById(Long id, UpdateOrderRequest updateOrderRequest);

    void deleteOrderById(Long id);

}
