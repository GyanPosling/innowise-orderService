package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServiceUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5632712394425044638L;

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
