package com.mukha.orderservice.security;

import com.mukha.orderservice.client.UserServiceClient;
import com.mukha.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {
    private final UserServiceClient userServiceClient;
    private final OrderService orderService;

    public boolean isOwner(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        String currentUserUuid = jwt.getClaim("sub");
        if (currentUserUuid == null) {
            return false;
        }
        Long orderUserId = orderService.getUserIdByOrderId(orderId);
        String dbUserUuid = String.valueOf(userServiceClient.getUserById(orderUserId).keycloakUUID());
        return currentUserUuid.equals(dbUserUuid);
    }
}
