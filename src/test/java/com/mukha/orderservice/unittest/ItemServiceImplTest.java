package com.mukha.orderservice.unittest;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.dto.response.ItemResponse;
import com.mukha.orderservice.exception.ItemNotFoundException;
import com.mukha.orderservice.mapper.ItemMapper;
import com.mukha.orderservice.model.Item;
import com.mukha.orderservice.repository.ItemRepository;
import com.mukha.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Laptop";
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(999.99);

    private Item item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        itemRequest = new ItemRequest(ITEM_NAME, ITEM_PRICE);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName(ITEM_NAME);
        item.setPrice(ITEM_PRICE);

        itemResponse = new ItemResponse(ITEM_ID, ITEM_NAME, ITEM_PRICE);
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        when(itemMapper.toEntity(itemRequest)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.createItem(itemRequest);

        assertThat(result).isEqualTo(itemResponse);
        verify(itemRepository).save(item);
        verify(itemMapper).toEntity(itemRequest);
        verify(itemMapper).toResponse(item);
    }

    @Test
    void getById_shouldReturnItemResponse_whenItemExists() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.getById(ITEM_ID);

        assertThat(result).isEqualTo(itemResponse);
        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void getById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(ITEM_ID))
                .isInstanceOf(ItemNotFoundException.class);

        verify(itemMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnPageOfItemResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(List.of(item), pageable, 1);

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        Page<ItemResponse> result = itemService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(itemResponse);
        verify(itemRepository).findAll(pageable);
    }

    @Test
    void updateById_shouldUpdateItemSuccessfully_whenItemExists() {
        ItemRequest updateRequest = new ItemRequest("Updated Name", BigDecimal.valueOf(1050.00));

        Item updatedItem = new Item();
        updatedItem.setId(ITEM_ID);
        updatedItem.setName("Updated Name");
        updatedItem.setPrice(BigDecimal.valueOf(1050.00));

        ItemResponse updatedResponse = new ItemResponse(ITEM_ID, "Updated Name", BigDecimal.valueOf(1050.00));

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemMapper.toEntityUpdate(updateRequest, item)).thenReturn(updatedItem);
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);
        when(itemMapper.toResponse(updatedItem)).thenReturn(updatedResponse);

        ItemResponse result = itemService.updateById(ITEM_ID, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
        verify(itemRepository).findById(ITEM_ID);
        verify(itemMapper).toEntityUpdate(updateRequest, item);
        verify(itemRepository).save(updatedItem);
    }

    @Test
    void updateById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateById(ITEM_ID, itemRequest))
                .isInstanceOf(ItemNotFoundException.class);

        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toEntityUpdate(any(), any());
    }

    @Test
    void deleteById_shouldDeleteItem_whenItemExists() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        itemService.deleteById(ITEM_ID);

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteById(ITEM_ID))
                .isInstanceOf(ItemNotFoundException.class);

        verify(itemRepository, never()).delete(any());
    }
}
