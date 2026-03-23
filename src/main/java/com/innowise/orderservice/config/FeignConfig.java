package com.innowise.orderservice.config;

import feign.RequestInterceptor;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
@Configuration
public class FeignConfig {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_ROLE_HEADER = "X-USER-ROLE";
    private static final String USER_EMAIL_HEADER = "X-USER-EMAIL";
    private static final String USERNAME_HEADER = "X-USER-NAME";
    private static final String TS_HEADER = "X-TS";
    private static final String SIGN_HEADER = "X-SIGN";
    private static final String SIGN_ALGORITHM = "HmacSHA256";

    @Value("${gateway.internal-signing-secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return;
            }

            String userId = auth.getDetails() != null ? String.valueOf(auth.getDetails()) : "";
            String role = extractRole(auth.getAuthorities());
            String email = auth.getName();
            String username = auth.getName();
            String ts = String.valueOf(Instant.now().getEpochSecond());
            String method = template.method() != null ? template.method() : "";
            String path = template.path() != null ? template.path().split("\\?")[0] : "";

            template.header(USER_ID_HEADER, userId);
            template.header(USER_ROLE_HEADER, role);
            template.header(USER_EMAIL_HEADER, email);
            template.header(USERNAME_HEADER, username);
            template.header(TS_HEADER, ts);
            template.header(SIGN_HEADER, sign(String.join(
                    "|",
                    method,
                    path,
                    userId,
                    role,
                    email,
                    username,
                    ts
            )));
        };
    }

    private String extractRole(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("");
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(SIGN_ALGORITHM);
            mac.init(new SecretKeySpec(internalSecret.getBytes(StandardCharsets.UTF_8), SIGN_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Failed to sign Feign gateway headers", ex);
        }
    }
}
