package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.OrderStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for managing orders with filtering and soft delete.
 */
public interface OrderService {

    /**
     * Creates a new order with its items.
     *
     * @param request order data
     * @return created order
     */
    OrderResponse create(OrderCreateRequest request);

    /**
     * Returns a non-deleted order by id.
     *
     * @param id order id
     * @return order
     */
    OrderResponse getById(Long id);

    /**
     * Returns orders filtered by creation time and statuses.
     *
     * @param createdFrom start of creation time range (inclusive)
     * @param createdTo end of creation time range (inclusive)
     * @param statuses allowed statuses
     * @param includeDeleted whether to include soft-deleted orders
     * @param pageable paging parameters
     * @return page of orders
     */
    Page<OrderResponse> getAll(Instant createdFrom, Instant createdTo, Collection<OrderStatus> statuses, boolean includeDeleted,
                               Pageable pageable);

    /**
     * Returns orders by user id.
     *
     * @param userId user id
     * @param includeDeleted whether to include soft-deleted orders
     * @return list of orders
     */
    List<OrderResponse> getByUserId(Long userId, boolean includeDeleted);

    /**
     * Updates a non-deleted order.
     *
     * @param id order id
     * @param request fields to update
     * @return updated order
     */
    OrderResponse update(Long id, OrderUpdateRequest request);

    /**
     * Soft deletes an order.
     *
     * @param id order id
     */
    void delete(Long id);
}
