package com.mukha.orderservice.integrationtest;

import com.mukha.orderservice.dto.request.ItemRequest;
import com.mukha.orderservice.model.Item;
import com.mukha.orderservice.repository.ItemRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerTest  extends AbstractIntegrationTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    void cleanUp() {
        itemRepository.deleteAll();
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return itemRepository.save(item);
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createItem_shouldReturnCreatedItem_whenRequestIsValid() throws Exception {
        ItemRequest request = new ItemRequest("Keyboard", BigDecimal.valueOf(49.99));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createItem_shouldReturnBadRequest_whenNameIsInvalid() throws Exception {
        ItemRequest request = new ItemRequest("", BigDecimal.valueOf(49.99));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void createItem_shouldReturnBadRequest_whenPriceIsNegative() throws Exception {
        ItemRequest request = new ItemRequest("KeyBoard", BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "user")
    void createItem_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        ItemRequest request = new ItemRequest("Keyboard", BigDecimal.valueOf(49.99));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getById_shouldReturnItem_whenItemExists() throws Exception {
        Item item = createItem("Keyboard", BigDecimal.valueOf(89.99));

        mockMvc.perform(get("/api/items/{id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(89.99));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/items/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void getAll_shouldReturnPageOfItems() throws Exception {
        createItem("Item 1", BigDecimal.valueOf(10));
        createItem("Item 2", BigDecimal.valueOf(20));

        mockMvc.perform(get("/api/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }


    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnUpdatedItem_whenItemExists() throws Exception {
        Item item = createItem("Keyboard", BigDecimal.valueOf(50.00));
        ItemRequest request = new ItemRequest("Updated", BigDecimal.valueOf(60.00));

        mockMvc.perform(patch("/api/items/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.price").value(60.00));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void updateById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        ItemRequest request = new ItemRequest("Updated", BigDecimal.valueOf(60.00));

        mockMvc.perform(patch("/api/items/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void deleteById_shouldReturnNoContent_whenItemExists() throws Exception {
        Item item = createItem("Keyboard", BigDecimal.valueOf(15.00));

        mockMvc.perform(delete("/api/items/{id}", item.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/{id}", item.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin")
    void deleteById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}