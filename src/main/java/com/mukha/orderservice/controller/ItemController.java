package com.mukha.orderservice.controller;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.dto.response.ItemResponse;
import com.mukha.orderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest itemRequest) {
        ItemResponse createdItem = itemService.createItem(itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<ItemResponse>> getAll(Pageable pageable) {
        Page<ItemResponse> items = itemService.getAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(items);

    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ItemResponse> updateById(@PathVariable Long id,
                                                   @Valid @RequestBody ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.updateById(id, itemRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        itemService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
