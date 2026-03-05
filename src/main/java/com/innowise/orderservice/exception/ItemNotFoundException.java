package com.innowise.orderservice.exception;

import java.io.Serial;

public class ItemNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 3731458919339112936L;

    public ItemNotFoundException() {
        super("Item not found");
    }

    public ItemNotFoundException(Long id) {
        super("Item not found with id: " + id);
    }

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
