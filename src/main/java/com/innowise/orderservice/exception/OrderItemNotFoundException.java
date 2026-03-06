package com.innowise.orderservice.exception;

import java.io.Serial;

public class OrderItemNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 4018192233590308521L;

    public OrderItemNotFoundException() {
        super("Order item not found");
    }

    public OrderItemNotFoundException(Long id) {
        super("Order item not found with id: " + id);
    }

    public OrderItemNotFoundException(String message) {
        super(message);
    }

    public OrderItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
