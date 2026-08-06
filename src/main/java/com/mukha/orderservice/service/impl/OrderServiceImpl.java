package com.mukha.orderservice.service.impl;

import com.mukha.orderservice.client.UserServiceClient;
import com.mukha.orderservice.dto.request.CreateOrderRequest;
import com.mukha.orderservice.dto.request.OrderItemRequest;
import com.mukha.orderservice.dto.request.UpdateOrderRequest;
import com.mukha.orderservice.dto.response.OrderResponse;
import com.mukha.orderservice.dto.response.UserResponse;
import com.mukha.orderservice.exception.ItemNotFoundException;
import com.mukha.orderservice.exception.OrderNotFoundException;
import com.mukha.orderservice.mapper.OrderMapper;
import com.mukha.orderservice.model.Item;
import com.mukha.orderservice.model.Order;
import com.mukha.orderservice.model.OrderItem;
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.repository.ItemRepository;
import com.mukha.orderservice.repository.OrderRepository;
import com.mukha.orderservice.service.OrderService;
import com.mukha.orderservice.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, String userEmail) {
        log.debug("Creating new order for user email: {}", userEmail);
        UserResponse userResponse = userServiceClient.getUserByEmail(userEmail);
        Order order = orderMapper.toEntity(createOrderRequest);
        order.setUserId(userResponse.id());

        enrichAndSetOrderItems(order, createOrderRequest.orderItems());
        BigDecimal totalPrice = calculateTotalPrice(order.getOrderItems());
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder, userResponse);
    }

    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getById(Long id) {
        log.debug("Fetching order by id: {}", id);
        Order foundOrder = getOrderEntityById(id);
        UserResponse userResponse = userServiceClient.getUserById(foundOrder.getUserId());
        return orderMapper.toResponse(foundOrder, userResponse);
    }


    public Page<OrderResponse> getAll(Long userId, LocalDateTime startDate, LocalDateTime endDate, List<OrderStatus> orderStatuses, Pageable pageable) {
        log.debug("Fetching pageable orders. Filters - userId: {}, startDate: {}, endDate: {}, statuses: {}", userId, startDate, endDate, orderStatuses);

        Specification<Order> spec = Specification.where(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.createdWithinRange(startDate, endDate))
                .and(OrderSpecification.hasStatuses(orderStatuses));
        Page<Order> foundOrders = orderRepository.findAll(spec, pageable);

        return foundOrders.map(orderMapper::toResponse);
    }

    @CachePut(value = "orders", key = "#id")
    @Transactional
    public OrderResponse updateById(Long id, UpdateOrderRequest updateOrderRequest) {
        log.debug("Updating order with id: {}", id);
        Order foundOrder = getOrderEntityById(id);
        orderMapper.updateEntityFromDto(updateOrderRequest, foundOrder);
        enrichAndSetOrderItems(foundOrder, updateOrderRequest.orderItems());
        BigDecimal totalPrice = calculateTotalPrice(foundOrder.getOrderItems());
        foundOrder.setTotalPrice(totalPrice);

        UserResponse userResponse = userServiceClient.getUserById(foundOrder.getUserId());
        Order updatedOrder = orderRepository.save(foundOrder);
        return orderMapper.toResponse(updatedOrder, userResponse);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrderById(Long id) {
        log.debug("Deleting order with id: {}", id);
        Order foundOrder = getOrderEntityById(id);
        orderRepository.delete(foundOrder);
    }

    private Order getOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order with id: {} not found", id);
                    return new OrderNotFoundException(id);
                });
    }

    private void enrichAndSetOrderItems(Order order, List<OrderItemRequest> requests) {
        List<OrderItem> newItems = new ArrayList<>();

        for (OrderItemRequest request : requests) {
            Item realItem = itemRepository.findById(request.itemId())
                    .orElseThrow(() -> new ItemNotFoundException(request.itemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(realItem);
            orderItem.setQuantity(request.quantity());

            newItems.add(orderItem);
        }

        order.getOrderItems().clear();
        order.getOrderItems().addAll(newItems);
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(this::calculateOrderItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateOrderItemPrice(OrderItem orderItem) {
        return orderItem.getItem()
                .getPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }
}