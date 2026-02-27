package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5422315528251070343L;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
