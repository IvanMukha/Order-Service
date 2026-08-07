package com.mukha.orderservice.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends GlobalServiceException{
    public UserNotFoundException(Long id) {
        super("User for order with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
