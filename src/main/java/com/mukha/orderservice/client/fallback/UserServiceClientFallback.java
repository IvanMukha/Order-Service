package com.mukha.orderservice.client.fallback;

import com.mukha.orderservice.client.UserServiceClient;
import com.mukha.orderservice.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserByEmail(String email) {
        log.error("User Service is unavailable. Fallback applied for email: {}", email);
        return new UserResponse(null, "Unknown", "User", email);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.error("User Service is unavailable. Fallback applied for user id: {}", id);
        return new UserResponse(id, "Unknown", "User", "Empty");
    }
}
