package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    Item toEntity(ItemCreateRequest request);

    ItemResponse toResponse(Item item);
}
