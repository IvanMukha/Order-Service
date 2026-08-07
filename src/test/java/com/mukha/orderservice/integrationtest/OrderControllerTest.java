package com.mukha.orderservice.integrationtest;

import com.mukha.orderservice.client.UserServiceClient;
import com.mukha.orderservice.dto.request.CreateOrderRequest;
import com.mukha.orderservice.dto.request.OrderItemRequest;
import com.mukha.orderservice.dto.request.UpdateOrderRequest;
import com.mukha.orderservice.dto.response.UserResponse;
import com.mukha.orderservice.model.Item;
import com.mukha.orderservice.model.Order;
import com.mukha.orderservice.model.OrderItem;
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.repository.ItemRepository;
import com.mukha.orderservice.repository.OrderRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "ivan@gmail.com";
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return itemRepository.save(item);
    }

    private Order createOrder(Long userId, OrderStatus status, Item item, int quantity) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setDeleted(false);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(quantity);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(quantity)));

        return orderRepository.save(order);
    }

    private UserResponse defaultUserResponse() {
        return new UserResponse(USER_ID, UUID.randomUUID(),"Ivan", "Mukha", USER_EMAIL);
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createOrder_shouldReturnCreatedOrder_whenRequestIsValid() throws Exception {
        Item item = createItem("Keyboard", BigDecimal.valueOf(50));
        when(userServiceClient.getUserByEmail(USER_EMAIL)).thenReturn(defaultUserResponse());

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(item.getId(), 2)));

        mockMvc.perform(post("/api/orders")
                        .param("userEmail", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.totalPrice").value(100))
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.user.email").value(USER_EMAIL));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createOrder_shouldReturnBadRequest_whenOrderItemsIsEmpty() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(List.of());

        mockMvc.perform(post("/api/orders")
                        .param("userEmail", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createOrder_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        when(userServiceClient.getUserByEmail(USER_EMAIL)).thenReturn(defaultUserResponse());

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(999L, 1)));

        mockMvc.perform(post("/api/orders")
                        .param("userEmail", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createOrder_shouldReturnBadRequest_whenQuantityIsLessThanOne() throws Exception {
        Item item = createItem("Mouse", BigDecimal.valueOf(20));

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(item.getId(), 0)));

        mockMvc.perform(post("/api/orders")
                        .param("userEmail", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getById_shouldReturnOrder_whenOrderExists() throws Exception {
        Item item = createItem("Monitor", BigDecimal.valueOf(200));
        Order order = createOrder(USER_ID, OrderStatus.CREATED, item, 1);
        when(userServiceClient.getUserById(USER_ID)).thenReturn(defaultUserResponse());

        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$.totalPrice").value(200));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getById_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getAll_shouldReturnAllOrders_whenNoFiltersProvided() throws Exception {
        Item item = createItem("Chair", BigDecimal.valueOf(80));
        createOrder(USER_ID, OrderStatus.CREATED, item, 1);
        createOrder(USER_ID, OrderStatus.CONFIRMED, item, 2);
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(List.of(defaultUserResponse()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getAll_shouldReturnFilteredOrders_whenStatusProvided() throws Exception {
        Item item = createItem("Desk", BigDecimal.valueOf(150));
        createOrder(USER_ID, OrderStatus.CREATED, item, 1);
        createOrder(USER_ID, OrderStatus.CANCELLED, item, 1);
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(List.of(defaultUserResponse()));

        mockMvc.perform(get("/api/orders").param("statuses", OrderStatus.CANCELLED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value(OrderStatus.CANCELLED.name()));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getAll_shouldReturnEmptyPage_whenUserIdDoesNotMatchAnyOrder() throws Exception {
        Item item = createItem("Lamp", BigDecimal.valueOf(30));
        createOrder(USER_ID, OrderStatus.CREATED, item, 1);

        mockMvc.perform(get("/api/orders").param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnUpdatedOrder_whenRequestIsValid() throws Exception {
        Item item = createItem("Headset", BigDecimal.valueOf(60));
        Order order = createOrder(USER_ID, OrderStatus.CREATED, item, 1);
        when(userServiceClient.getUserById(USER_ID)).thenReturn(defaultUserResponse());

        UpdateOrderRequest request = new UpdateOrderRequest(
                OrderStatus.CONFIRMED, List.of(new OrderItemRequest(item.getId(), 3)));

        mockMvc.perform(patch("/api/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.name()))
                .andExpect(jsonPath("$.totalPrice").value(180))
                .andExpect(jsonPath("$.orderItems.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        UpdateOrderRequest request = new UpdateOrderRequest(
                OrderStatus.CONFIRMED, List.of(new OrderItemRequest(1L, 1)));

        mockMvc.perform(patch("/api/orders/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnBadRequest_whenStatusIsNull() throws Exception {
        Item item = createItem("Webcam", BigDecimal.valueOf(40));
        Order order = createOrder(USER_ID, OrderStatus.CREATED, item, 1);

        UpdateOrderRequest request = new UpdateOrderRequest(
                null, List.of(new OrderItemRequest(item.getId(), 1)));

        mockMvc.perform(patch("/api/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnBadRequest_whenOrderItemsIsEmpty() throws Exception {
        Item item = createItem("Cable", BigDecimal.valueOf(10));
        Order order = createOrder(USER_ID, OrderStatus.CREATED, item, 1);

        UpdateOrderRequest request = new UpdateOrderRequest(OrderStatus.CONFIRMED, List.of());

        mockMvc.perform(patch("/api/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void deleteOrderById_shouldReturnNoContent_whenOrderExists() throws Exception {
        Item item = createItem("Speaker", BigDecimal.valueOf(70));
        Order order = createOrder(USER_ID, OrderStatus.CREATED, item, 1);

        mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void deleteOrderById_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}

