package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import java.util.List;


/**
 * Service for managing order items.
 */
public interface OrderItemService {
    
    /**
     * Creates a new order item.
     *
     * @param orderItem order item to create
     * @return created order item
     */
    OrderItemResponseDto create(Long orderId, OrderItemCreateRequest orderItem);

    /**
     * Returns an order item by id.
     *
     * @param id order item id
     * @return order item
     */
    OrderItemResponseDto getById(Long orderId, Long id);

    /**
     * Returns all order items.
     *
     * @return list of order items
     */
    List<OrderItemResponseDto> getAll(Long orderId);

    /**
     * Updates an order item by id.
     *
     * @param id order item id
     * @param orderItem fields to update
     * @return updated order item
     */
    OrderItemResponseDto update(Long orderId, Long id, OrderItemUpdateRequest orderItem);

    /**
     * Deletes an order item by id.
     *
     * @param id order item id
     */
    void delete(Long orderId, Long id);
}
