package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public Item toEntity(ItemCreateRequest request) {
        if (request == null) {
            return null;
        }
        Item item = new Item();
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        return item;
    }

    public ItemResponse toResponse(Item item) {
        if (item == null) {
            return null;
        }
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .build();
    }
}
