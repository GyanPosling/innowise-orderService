package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(OrderItemCreateRequest request, Order order, Item item) {
        if (request == null) {
            return null;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(request.getQuantity());
        return orderItem;
    }

    public OrderItemResponseDto toResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        Long orderId = orderItem.getOrder() != null ? orderItem.getOrder().getId() : null;
        Long itemId = orderItem.getItem() != null ? orderItem.getItem().getId() : null;
        return OrderItemResponseDto.builder()
                .id(orderItem.getId())
                .orderId(orderId)
                .itemId(itemId)
                .quantity(orderItem.getQuantity())
                .build();
    }
}
