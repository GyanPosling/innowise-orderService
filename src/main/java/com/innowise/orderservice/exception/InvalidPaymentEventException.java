package com.innowise.orderservice.exception;

public class InvalidPaymentEventException extends RuntimeException {

    public InvalidPaymentEventException(String message) {
        super(message);
    }
}
