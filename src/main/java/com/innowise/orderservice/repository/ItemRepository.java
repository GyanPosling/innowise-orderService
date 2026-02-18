package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Item;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);

    List<Item> findAllByIdInAndDeletedAtIsNull(Collection<Long> ids);
}
