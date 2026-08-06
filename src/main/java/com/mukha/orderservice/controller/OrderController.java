package com.mukha.orderservice.controller;

import com.mukha.orderservice.dto.request.CreateOrderRequest;
import com.mukha.orderservice.dto.request.UpdateOrderRequest;
import com.mukha.orderservice.dto.response.OrderResponse;
import com.mukha.orderservice.model.status.OrderStatus;
import com.mukha.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestParam String userEmail, @Valid @RequestBody CreateOrderRequest createOrderRequest){
        OrderResponse savedOrder=orderService.createOrder(createOrderRequest,userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAll(@RequestParam(required = false)Long userId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                      @RequestParam(required = false) List<OrderStatus> statuses,
                                                      Pageable pageable){
        Page<OrderResponse> orders = orderService.getAll(userId, startDate, endDate, statuses, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(orders);

    }
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateById(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateOrderRequest updateOrderRequest){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.updateById(id,updateOrderRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id){
        orderService.deleteOrderById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
