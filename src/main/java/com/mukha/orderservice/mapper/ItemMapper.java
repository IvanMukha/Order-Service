package com.mukha.orderservice.mapper;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.dto.response.ItemResponse;
import com.mukha.orderservice.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {

            ItemResponse toResponse(Item item);

            Item toEntity(ItemRequest itemRequest);

            Item toEntity(ItemResponse itemResponse);

            Item toEntityUpdate(ItemRequest itemRequest, @MappingTarget Item  item);
}
