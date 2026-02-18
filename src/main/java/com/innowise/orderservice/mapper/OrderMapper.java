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
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(OrderCreateRequest request, Map<Long, Item> itemsById) {
        if (request == null) {
            return null;
        }
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setUserEmail(request.getUserEmail());
        order.setStatus(request.getStatus());
        order.setTotalPrice(request.getTotalPrice());
        order.setOrderItems(toOrderItems(order, request.getItems(), itemsById));
        return order;
    }

    public List<OrderItem> toOrderItems(Order order, List<OrderItemRequest> itemRequests, Map<Long, Item> itemsById) {
        if (itemRequests == null) {
            return new ArrayList<>();
        }
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            items.add(toOrderItem(order, itemRequest, itemsById.get(itemRequest.getItemId())));
        }
        return items;
    }

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemResponse> items = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deletedAt(order.getDeletedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    private OrderItem toOrderItem(Order order, OrderItemRequest itemRequest, Item item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setItem(item);
        return orderItem;
    }

    private OrderItemResponse toResponse(OrderItem orderItem) {
        Long itemId = orderItem.getItem() != null ? orderItem.getItem().getId() : null;
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .itemId(itemId)
                .quantity(orderItem.getQuantity())
                .build();
    }
}


