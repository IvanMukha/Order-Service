package com.mukha.orderservice.unittest;

import com.mukha.orderservice.client.UserServiceClient;
import com.mukha.orderservice.dto.response.UserResponse;
import com.mukha.orderservice.security.OrderSecurity;
import com.mukha.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSecurityTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderService orderService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private OrderSecurity orderSecurity;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserIsOwner() {
        Long orderId = 1L;
        Long orderUserId = 42L;
        String userUuid = UUID.randomUUID().toString();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("sub")).thenReturn(userUuid);
        when(orderService.getUserIdByOrderId(orderId)).thenReturn(orderUserId);

        UserResponse mockUser = Mockito.mock(UserResponse.class);
        when(mockUser.keycloakUUID()).thenReturn(UUID.fromString(userUuid));
        when(userServiceClient.getUserById(orderUserId)).thenReturn(mockUser);

        boolean result = orderSecurity.isOwner(orderId);

        assertTrue(result);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenUserIsNotOwner() {
        Long orderId = 1L;
        Long orderUserId = 42L;
        String currentUserUuid = UUID.randomUUID().toString();
        String differentUserUuid = UUID.randomUUID().toString();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("sub")).thenReturn(currentUserUuid);
        when(orderService.getUserIdByOrderId(orderId)).thenReturn(orderUserId);

        UserResponse mockUser = Mockito.mock(UserResponse.class);
        when(mockUser.keycloakUUID()).thenReturn(UUID.fromString(differentUserUuid));
        when(userServiceClient.getUserById(orderUserId)).thenReturn(mockUser);

        boolean result = orderSecurity.isOwner(orderId);

        assertFalse(result);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = orderSecurity.isOwner(1L);

        assertFalse(result);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenPrincipalIsNotJwt() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt-instance");

        boolean result = orderSecurity.isOwner(1L);

        assertFalse(result);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenSubClaimMissing() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("sub")).thenReturn(null);

        boolean result = orderSecurity.isOwner(1L);

        assertFalse(result);
    }
}
