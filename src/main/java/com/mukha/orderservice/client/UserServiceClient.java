package com.mukha.orderservice.client;

import com.mukha.orderservice.client.fallback.UserServiceClientFallback;
import com.mukha.orderservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "${services.user-service.name}",
        url = "${services.user-service.url}",
        fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/v1/api/users/email")
    UserResponse getUserByEmail(@RequestParam String email);

    @GetMapping("/v1/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

    @GetMapping("/v1/api/users/batch")
    List<UserResponse> getUsersByIds(@RequestParam("ids") List<Long> userIds);
}
