package com.innowise.orderservice.config.security;

import com.innowise.orderservice.model.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_ROLE_HEADER = "X-USER-ROLE";
    private static final String USER_EMAIL_HEADER = "X-USER-EMAIL";
    private static final String USERNAME_HEADER = "X-USER-NAME";
    private static final String TS_HEADER = "X-TS";
    private static final String SIGN_HEADER = "X-SIGN";
    private static final String SIGN_ALGORITHM = "HmacSHA256";

    @Value("${gateway.internal-signing-secret}")
    private String internalSecret;

    @Value("${internal.ttl-seconds:60}")
    private long ttlSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String roleHeader = request.getHeader(USER_ROLE_HEADER);
            String email = request.getHeader(USER_EMAIL_HEADER);
            String username = request.getHeader(USERNAME_HEADER);
            String userId = request.getHeader(USER_ID_HEADER);
            String ts = request.getHeader(TS_HEADER);
            String signature = request.getHeader(SIGN_HEADER);
            String method = request.getMethod();
            String path = request.getRequestURI();

            if (isSignatureValid(userId, roleHeader, email, username, method, path, ts, signature)
                    && roleHeader != null
                    && (email != null || username != null)) {
                setAuthentication(userId, roleHeader, email, username);
            } else if (roleHeader != null || signature != null || ts != null) {
                log.debug("Rejected request with invalid gateway signature");
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String userId, String roleHeader, String email, String username) {
        Role role;
        try {
            role = Role.valueOf(roleHeader);
        } catch (IllegalArgumentException ex) {
            log.debug("Rejected request with unsupported role header: {}", roleHeader);
            return;
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        String principal = email != null ? email : username;
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, userId,
                        Collections.singletonList(authority));
        if (userId != null) {
            try {
                authentication.setDetails(UUID.fromString(userId));
            } catch (IllegalArgumentException ex) {
                log.debug("Rejected request with malformed user id header: {}", userId);
                return;
            }
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isSignatureValid(String userId,
                                     String role,
                                     String email,
                                     String username,
                                     String method,
                                     String path,
                                     String tsHeader,
                                     String signature) {
        if (internalSecret == null || internalSecret.isBlank()) {
            return false;
        }
        if (signature == null || tsHeader == null || ttlSeconds <= 0) {
            return false;
        }
        long ts;
        try {
            ts = Long.parseLong(tsHeader);
        } catch (NumberFormatException ex) {
            return false;
        }
        long now = Instant.now().getEpochSecond();
        if (ts > now || now - ts > ttlSeconds) {
            return false;
        }
        String payload = String.join(
                "|",
                emptyIfNull(method),
                emptyIfNull(path),
                emptyIfNull(userId),
                emptyIfNull(role),
                emptyIfNull(email),
                emptyIfNull(username),
                tsHeader
        );
        String expected = sign(payload);
        if (expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(SIGN_ALGORITHM);
            mac.init(new SecretKeySpec(internalSecret.getBytes(StandardCharsets.UTF_8), SIGN_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return null;
        }
    }
}
