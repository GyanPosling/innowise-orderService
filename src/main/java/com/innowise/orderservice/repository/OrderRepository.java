package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findAllByUserId(Long userId);

    List<Order> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
}

