package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.entity.Role;
import com.innowise.orderservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void validateToken(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new JwtException("Token expiration is missing");
        }
        if (expiration.before(new Date())) {
            throw new JwtException("Token is expired");
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    @Override
    public UUID extractUserId(String token) {
        return extractClaim(token, claims -> {
            String userId = claims.get("userId", String.class);
            return userId == null ? null : UUID.fromString(userId);
        });
    }

    @Override
    public Role extractRole(String token) {
        return extractClaim(token, claims -> Role.valueOf(claims.get("role", String.class)));
    }

    private <T> T extractClaim(String token, Function<Claims, T> function) {
        Claims claims = extractAllClaims(token);
        return function.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
