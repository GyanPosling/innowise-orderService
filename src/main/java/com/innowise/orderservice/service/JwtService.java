package com.innowise.orderservice.service;

import com.innowise.orderservice.model.entity.Role;
import java.util.UUID;

public interface JwtService {
    void validateToken(String token);

    String extractUsername(String token);

    String extractEmail(String token);

    UUID extractUserId(String token);

    Role extractRole(String token);
}
