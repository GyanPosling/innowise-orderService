package com.innowise.orderservice.exception;

import java.io.Serial;

public class OrderNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1270332516724957011L;

    public OrderNotFoundException() {
        super("Order not found");
    }

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
