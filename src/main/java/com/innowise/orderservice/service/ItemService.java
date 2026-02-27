package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import java.util.List;

/**
 * Service for managing items.
 */
public interface ItemService {

    /**
     * Creates a new item.
     *
     * @param item item to create
     * @return created item
     */
    ItemResponse create(ItemCreateRequest item);

    /**
     * Returns an item by id.
     *
     * @param id item id
     * @return item
     */
    ItemResponse getById(Long id);

    /**
     * Returns all items.
     *
     * @return list of items
     */
    List<ItemResponse> getAll();

    /**
     * Updates an item by id.
     *
     * @param id item id
     * @param item fields to update
     * @return updated item
     */
    ItemResponse update(Long id, ItemUpdateRequest item);

    /**
     * Deletes an item by id.
     *
     * @param id item id
     */
    void delete(Long id);
}
