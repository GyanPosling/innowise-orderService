package com.innowise.orderservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Configuration
public class FeignConfig {

    private static final List<String> SIGNED_HEADERS = List.of(
            "X-USER-ID",
            "X-USER-ROLE",
            "X-USER-EMAIL",
            "X-USER-NAME",
            "X-TS",
            "X-SIGN"
    );

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            for (String header : SIGNED_HEADERS) {
                String value = request.getHeader(header);
                if (value != null) {
                    template.header(header, value);
                }
            }
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                template.header("Authorization", authorization);
            }
        };
    }
}
