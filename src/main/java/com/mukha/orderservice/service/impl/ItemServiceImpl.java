package com.mukha.orderservice.service.impl;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.dto.response.ItemResponse;
import com.mukha.orderservice.exception.ItemNotFoundException;
import com.mukha.orderservice.mapper.ItemMapper;
import com.mukha.orderservice.model.Item;
import com.mukha.orderservice.repository.ItemRepository;
import com.mukha.orderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponse createItem(ItemRequest itemRequest) {
        log.debug("Attempting to save new Item");
        Item createdItem = itemRepository.save(itemMapper.toEntity(itemRequest));
        log.debug("Successfully create item with id: {}", createdItem.getId());
        return itemMapper.toResponse(createdItem);
    }

    public ItemResponse getById(Long id) {
        log.debug("Fetching item by id: {}", id);
        return itemMapper.toResponse(getItemEntityById(id));
    }

    public Page<ItemResponse> getAll(Pageable pageable) {
        log.debug("Fetching pageable items");
        Page<Item> foundItems = itemRepository.findAll(pageable);
        return foundItems.map(itemMapper::toResponse);
    }

    @Transactional
    public ItemResponse updateById(Long id, ItemRequest itemRequest) {
        log.debug("Updating item by id: {}", id);
        Item foundItem = getItemEntityById(id);
        Item updatedItem = itemMapper.toEntityUpdate(itemRequest, foundItem);
        ItemResponse result = itemMapper.toResponse(itemRepository.save(updatedItem));
        log.debug("Successfully updated item with id: {}", id);
        return result;
    }

    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting item by id: {}", id);
        Item foundItem = getItemEntityById(id);
        itemRepository.delete(foundItem);
    }

    private Item getItemEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Item with id: {} not found", id);
                    return new ItemNotFoundException(id);
                });
    }
}
