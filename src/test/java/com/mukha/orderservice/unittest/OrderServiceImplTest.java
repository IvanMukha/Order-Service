package com.mukha.orderservice.unittest;

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
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.repository.ItemRepository;
import com.mukha.orderservice.repository.OrderRepository;
import com.mukha.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final String USER_EMAIL = "ivan@gmail.com";

    private UserResponse userResponse;
    private Item item;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(USER_ID, UUID.randomUUID(), "Ivan", "Mukha", USER_EMAIL);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test item");
        item.setPrice(BigDecimal.valueOf(50));
    }

    @Test
    void createOrder_shouldCreateOrderWithCorrectTotalPrice() {
        OrderItemRequest itemRequest = new OrderItemRequest(ITEM_ID, 3);
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        Order mappedOrder = new Order();
        mappedOrder.setOrderItems(new java.util.ArrayList<>());

        Order savedOrder = new Order();
        savedOrder.setId(ORDER_ID);
        savedOrder.setUserId(USER_ID);
        savedOrder.setOrderItems(new java.util.ArrayList<>());

        OrderResponse expectedResponse = new OrderResponse(
                ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.valueOf(150), List.of(), userResponse);

        when(userServiceClient.getUserByEmail(USER_EMAIL)).thenReturn(userResponse);
        when(orderMapper.toEntity(request)).thenReturn(mappedOrder);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder, userResponse)).thenReturn(expectedResponse);

        OrderResponse result = orderService.createOrder(request, USER_EMAIL);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order orderToSave = orderCaptor.getValue();

        assertThat(orderToSave.getUserId()).isEqualTo(USER_ID);
        assertThat(orderToSave.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(orderToSave.getOrderItems()).hasSize(1);
        assertThat(orderToSave.getOrderItems().getFirst().getItem()).isEqualTo(item);
        assertThat(orderToSave.getOrderItems().getFirst().getQuantity()).isEqualTo(3);
    }

    @Test
    void createOrder_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        OrderItemRequest itemRequest = new OrderItemRequest(ITEM_ID, 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        Order mappedOrder = new Order();
        mappedOrder.setOrderItems(new java.util.ArrayList<>());

        when(userServiceClient.getUserByEmail(USER_EMAIL)).thenReturn(userResponse);
        when(orderMapper.toEntity(request)).thenReturn(mappedOrder);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request, USER_EMAIL))
                .isInstanceOf(ItemNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnOrder_whenExists() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);

        OrderResponse expectedResponse = new OrderResponse(
                ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.TEN, List.of(), userResponse);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(USER_ID)).thenReturn(userResponse);
        when(orderMapper.toResponse(order, userResponse)).thenReturn(expectedResponse);

        OrderResponse result = orderService.getById(ORDER_ID);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getById_shouldThrowOrderNotFoundException_whenNotExists() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class);

        verify(userServiceClient, never()).getUserById(any());
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoOrdersFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<OrderResponse> result = orderService.getAll(USER_ID, null, null, null, pageable);

        assertThat(result).isEmpty();
        verify(userServiceClient, never()).getUsersByIds(any());
    }

    @Test
    void getAll_shouldReturnPageOfOrders_whenFound() {
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);

        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        OrderResponse expectedResponse = new OrderResponse(
                ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.TEN, List.of(), userResponse);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.getUsersByIds(List.of(USER_ID))).thenReturn(List.of(userResponse));
        when(orderMapper.toResponse(order, userResponse)).thenReturn(expectedResponse);

        Page<OrderResponse> result = orderService.getAll(
                null, LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(OrderStatus.CREATED), pageable);

        assertThat(result.getContent()).containsExactly(expectedResponse);
    }

    @Test
    void updateByIdshouldUpdateOrderSuccessfully() {
        OrderItemRequest itemRequest = new OrderItemRequest(ITEM_ID, 2);
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatus.CONFIRMED, List.of(itemRequest));

        Order existingOrder = new Order();
        existingOrder.setId(ORDER_ID);
        existingOrder.setUserId(USER_ID);
        existingOrder.setOrderItems(new java.util.ArrayList<>());

        OrderResponse expectedResponse = new OrderResponse(
                ORDER_ID, USER_ID, OrderStatus.CONFIRMED, BigDecimal.valueOf(100), List.of(), userResponse);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userServiceClient.getUserById(USER_ID)).thenReturn(userResponse);
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
        when(orderMapper.toResponse(existingOrder, userResponse)).thenReturn(expectedResponse);

        OrderResponse result = orderService.updateById(ORDER_ID, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(orderMapper).updateEntityFromDto(request, existingOrder);
        assertThat(existingOrder.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(existingOrder.getOrderItems()).hasSize(1);
    }

    @Test
    void updateById_shouldThrowOrderNotFoundException_whenNotExists() {
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatus.CONFIRMED, List.of());

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateById(ORDER_ID, request))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        OrderItemRequest itemRequest = new OrderItemRequest(ITEM_ID, 1);
        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatus.CONFIRMED, List.of(itemRequest));

        Order existingOrder = new Order();
        existingOrder.setId(ORDER_ID);
        existingOrder.setOrderItems(new java.util.ArrayList<>());

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateById(ORDER_ID, request))
                .isInstanceOf(ItemNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteById_shouldDeleteOrder_whenExists() {
        Order existingOrder = new Order();
        existingOrder.setId(ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        orderService.deleteOrderById(ORDER_ID);

        verify(orderRepository, times(1)).delete(existingOrder);
    }

    @Test
    void deleteById_shouldThrowOrderNotFoundException_whenNotExists() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrderById(ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).delete(any(Order.class));
    }
}
