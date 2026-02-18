package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3803330047762326672L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(long id) {
        super("Resource not found for id: " + id);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
