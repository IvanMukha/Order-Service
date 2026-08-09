package com.mukha.orderservice.service;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    ItemResponse createItem(ItemRequest itemRequest);

    ItemResponse getById(Long itemId);

    Page<ItemResponse> getAll(Pageable pageable);

    ItemResponse updateById(Long id, ItemRequest itemRequest);

    void deleteById(Long id);
}
