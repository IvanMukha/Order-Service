package com.mukha.orderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends GlobalServiceException {

    public OrderNotFoundException(Long id) {
        super("Order with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
