package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponse;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "processedPaymentIds", ignore = true)
    Order toEntity(OrderCreateRequest request, @Context Map<Long, Item> itemsById);

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "user", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponse toResponse(OrderItem orderItem);

    default OrderItem toOrderItem(
            Order order,
            OrderItemRequest itemRequest,
            @Context Map<Long, Item> itemsById
    ) {
        if (itemRequest == null) {
            return null;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setItem(itemsById.get(itemRequest.getItemId()));
        return orderItem;
    }

    default List<OrderItem> toOrderItems(
            Order order,
            List<OrderItemRequest> itemRequests,
            @Context Map<Long, Item> itemsById
    ) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRequests.stream()
                .map(itemRequest -> toOrderItem(order, itemRequest, itemsById))
                .collect(Collectors.toList());
    }

}

