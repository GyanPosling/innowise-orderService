package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderItemService;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderItemResponseDto create(Long orderId, OrderItemCreateRequest orderItem) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Item item = itemRepository.findByIdAndDeletedAtIsNull(orderItem.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(orderItem.getItemId()));
        OrderItem entity = orderItemMapper.toEntity(orderItem, order, item);
        return orderItemMapper.toResponse(orderItemRepository.save(entity));
    }

    @Override
    public OrderItemResponseDto getById(Long orderId, Long id) {
        OrderItem entity = orderItemRepository.findByIdAndOrderIdAndDeletedAtIsNull(id, orderId)
                .orElseThrow(() -> new OrderItemNotFoundException(id));
        return orderItemMapper.toResponse(entity);
    }

    @Override
    public List<OrderItemResponseDto> getAll(Long orderId) {
        return orderItemRepository.findAllByOrderIdAndDeletedAtIsNull(orderId).stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderItemResponseDto update(Long orderId, Long id, OrderItemUpdateRequest orderItem) {
        OrderItem existing = orderItemRepository.findByIdAndOrderIdAndDeletedAtIsNull(id, orderId)
                .orElseThrow(() -> new OrderItemNotFoundException(id));

        if (orderItem.getItemId() != null) {
            Item item = itemRepository.findByIdAndDeletedAtIsNull(orderItem.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException(orderItem.getItemId()));
            existing.setItem(item);
        }
        if (orderItem.getQuantity() != null) {
            existing.setQuantity(orderItem.getQuantity());
        }

        return orderItemMapper.toResponse(orderItemRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long orderId, Long id) {
        OrderItem existing = orderItemRepository.findByIdAndOrderIdAndDeletedAtIsNull(id, orderId)
                .orElseThrow(() -> new OrderItemNotFoundException(id));
        orderItemRepository.delete(existing);
    }
}
