package com.mukha.orderservice.unittest;

import com.mukha.orderservice.client.fallback.UserServiceClientFallback;
import com.mukha.orderservice.dto.response.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserServiceClientFallbackTest {

    private final UserServiceClientFallback fallback = new UserServiceClientFallback();

    @Test
    void shouldReturnDefaultUserWhenGetByEmail() {
        String email = "MyEmail@gmail.com";

        UserResponse response = fallback.getUserByEmail(email);

        assertNotNull(response);
        assertNull(response.id());
        assertNotNull(response.keycloakUUID());
        assertEquals("Unknown", response.name());
        assertEquals(email, response.email());
    }
    @Test
    void shouldReturnDefaultUserWhenGetById() {
        Long id = 123L;

        UserResponse response = fallback.getUserById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Unknown", response.name());
    }

    @Test
    void shouldMapListWhenGetUsersByIds() {
        List<Long> ids = List.of(1L, 2L);

        List<UserResponse> responses = fallback.getUsersByIds(ids);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(2L, responses.get(1).id());
    }
}
