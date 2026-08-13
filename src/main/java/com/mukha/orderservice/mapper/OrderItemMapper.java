package com.mukha.orderservice.mapper;

import com.mukha.orderservice.dto.request.OrderItemRequest;
import com.mukha.orderservice.dto.response.OrderItemResponse;
import com.mukha.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponse toItemResponse(OrderItem orderItem);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", ignore = true)
    OrderItem toItemEntity(OrderItemRequest request);
}
