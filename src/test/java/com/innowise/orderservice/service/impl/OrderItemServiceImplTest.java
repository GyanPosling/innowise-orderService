package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.orderservice.exception.OrderItemNotFoundException;
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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void create_returnsResponse() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(1L)
                .itemId(2L)
                .quantity(3)
                .build();
        Order order = new Order();
        Item item = new Item();
        OrderItem entity = new OrderItem();
        OrderItemResponseDto response = OrderItemResponseDto.builder().id(1L).orderId(1L).itemId(2L).quantity(3).build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toEntity(request, order, item)).thenReturn(entity);
        when(orderItemRepository.save(entity)).thenReturn(entity);
        when(orderItemMapper.toResponse(entity)).thenReturn(response);

        OrderItemResponseDto result = orderItemService.create(request);

        assertEquals(1L, result.getId());
        verify(orderItemRepository).save(entity);
    }

    @Test
    void getById_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.getById(1L));
    }

    @Test
    void update_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder().quantity(2).build();
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.update(1L, request));
    }

    @Test
    void delete_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.delete(1L));
    }
}
