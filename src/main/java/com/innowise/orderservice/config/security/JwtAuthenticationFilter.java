package com.innowise.orderservice.config.security;

import com.innowise.orderservice.model.entity.Role;
import com.innowise.orderservice.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = getTokenFromRequest(request);
        if (jwtToken != null) {
            try {
                jwtService.validateToken(jwtToken);
                setAuthentication(jwtToken);
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Invalid JWT", ex);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            return requestTokenHeader.substring(7);
        }
        return null;
    }

    private void setAuthentication(String jwtToken) {
        String email = jwtService.extractEmail(jwtToken);
        Long userId = jwtService.extractUserId(jwtToken);
        Role role = jwtService.extractRole(jwtToken);

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, jwtToken, authorities);
        if (userId != null) {
            authentication.setDetails(userId);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
