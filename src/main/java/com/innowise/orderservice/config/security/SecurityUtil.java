package com.innowise.orderservice.config.security;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtil")
@RequiredArgsConstructor
public class SecurityUtil {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object details = auth.getDetails();
        if (details instanceof UUID userId) {
            return userId;
        }
        if (details instanceof String stringId) {
            try {
                return UUID.fromString(stringId);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public boolean isOwnerByOrderId(Long orderId) {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }
        Optional<Order> order = orderRepository.findByIdAndDeletedAtIsNull(orderId);
        return order.map(o -> userId.equals(o.getUserId())).orElse(false);
    }

    public boolean isOwnerByUserId(UUID userId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        return currentUserId.equals(userId);
    }
}
