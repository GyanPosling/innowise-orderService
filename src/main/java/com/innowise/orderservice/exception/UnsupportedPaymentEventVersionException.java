package com.innowise.orderservice.exception;

public class UnsupportedPaymentEventVersionException extends RuntimeException {

    public UnsupportedPaymentEventVersionException(String message) {
        super(message);
    }
}
