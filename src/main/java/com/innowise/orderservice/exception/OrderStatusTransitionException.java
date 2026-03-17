package com.innowise.orderservice.exception;

public class OrderStatusTransitionException extends RuntimeException {

    public OrderStatusTransitionException(String message) {
        super(message);
    }
}
