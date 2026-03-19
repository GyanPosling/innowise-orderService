package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.entity.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceImplTest {

    private static final String SECRET = "0123456789ABCDEF0123456789ABCDEF";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    void validateToken_shouldAcceptValidToken() {
        jwtService.validateToken(buildToken(Instant.now().plusSeconds(3600)));
    }

    @Test
    void validateToken_shouldRejectExpiredToken() {
        String expiredToken = buildToken(Instant.now().minusSeconds(60));

        assertThrows(JwtException.class, () -> jwtService.validateToken(expiredToken));
    }

    @Test
    void extractMethods_shouldReturnClaims() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000077");
        String token = buildToken(Instant.now().plusSeconds(3600), userId, "jwt-user@example.com", Role.ADMIN);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jwt-user@example.com");
        assertThat(jwtService.extractEmail(token)).isEqualTo("jwt-user@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.ADMIN);
    }

    private String buildToken(Instant expiration) {
        return buildToken(
                expiration,
                UUID.fromString("00000000-0000-0000-0000-000000000070"),
                "jwt-user@example.com",
                Role.USER
        );
    }

    private String buildToken(Instant expiration, UUID userId, String email, Role role) {
        Instant issuedAt = expiration.minusSeconds(300);
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("userId", userId.toString())
                .claim("role", role.name())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
