package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findAllByUserId(UUID userId);

    List<Order> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
}
