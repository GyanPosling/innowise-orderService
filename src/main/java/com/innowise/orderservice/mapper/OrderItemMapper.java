package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", source = "order")
    @Mapping(target = "item", source = "item")
    OrderItem toEntity(OrderItemCreateRequest request, Order order, Item item);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponseDto toResponse(OrderItem orderItem);
}
