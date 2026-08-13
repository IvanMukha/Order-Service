package com.mukha.orderservice.mapper;

import com.mukha.orderservice.dto.request.CreateOrderRequest;
import com.mukha.orderservice.dto.request.UpdateOrderRequest;
import com.mukha.orderservice.dto.response.OrderResponse;
import com.mukha.orderservice.dto.response.UserResponse;
import com.mukha.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = OrderItemMapper.class
)
public interface OrderMapper {
    @Mapping(target = "id", source = "order.id")
    OrderResponse toResponse(Order order, UserResponse user);

    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(CreateOrderRequest request);

    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(UpdateOrderRequest request);

    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDto(UpdateOrderRequest request, @MappingTarget Order order);
}