package com.mukha.orderservice.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends GlobalServiceException {

    public ItemNotFoundException(Long id) {
        super("Item with id " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
