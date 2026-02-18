package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.OrderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByIdAndDeletedAtIsNull(Long id);
}
