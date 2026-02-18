package com.innowise.orderservice.repository.specification;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public final class OrderSpecifications {

    public static Specification<Order> createdAtBetween(Instant from, Instant to) {
        return (root, query, builder) -> {
            if (from == null && to == null) {
                return builder.conjunction();
            }
            if (from != null && to != null) {
                return builder.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return builder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return builder.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Order> statusIn(Collection<OrderStatus> statuses) {
        return (root, query, builder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return builder.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }
}
