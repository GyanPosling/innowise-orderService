package com.innowise.orderservice.service;

import com.innowise.orderservice.model.entity.Role;

public interface JwtService {
    void validateToken(String token);

    String extractUsername(String token);

    String extractEmail(String token);

    Long extractUserId(String token);

    Role extractRole(String token);
}
