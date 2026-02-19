package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
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
    void create_shouldThrowException_whenOrderNotFound() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(1L)
                .itemId(2L)
                .quantity(3)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.create(request));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void create_shouldThrowException_whenItemNotFound() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(1L)
                .itemId(2L)
                .quantity(3)
                .build();
        Order order = new Order();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> orderItemService.create(request));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void getById_returnsResponse() {
        OrderItem orderItem = new OrderItem();
        OrderItemResponseDto response = OrderItemResponseDto.builder().id(1L).build();

        when(orderItemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toResponse(orderItem)).thenReturn(response);

        OrderItemResponseDto result = orderItemService.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.getById(1L));
    }

    @Test
    void getAll_returnsResponses() {
        OrderItem first = new OrderItem();
        OrderItem second = new OrderItem();
        OrderItemResponseDto response1 = OrderItemResponseDto.builder().id(1L).build();
        OrderItemResponseDto response2 = OrderItemResponseDto.builder().id(2L).build();

        when(orderItemRepository.findAll()).thenReturn(List.of(first, second));
        when(orderItemMapper.toResponse(first)).thenReturn(response1);
        when(orderItemMapper.toResponse(second)).thenReturn(response2);

        List<OrderItemResponseDto> results = orderItemService.getAll();

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    void update_shouldApplyProvidedFields() {
        OrderItem existing = new OrderItem();
        Order newOrder = new Order();
        Item newItem = new Item();
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder()
                .orderId(5L)
                .itemId(6L)
                .quantity(10)
                .build();
        OrderItemResponseDto response = OrderItemResponseDto.builder().id(1L).orderId(5L).itemId(6L).quantity(10).build();

        when(orderItemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(newOrder));
        when(itemRepository.findByIdAndDeletedAtIsNull(6L)).thenReturn(Optional.of(newItem));
        when(orderItemRepository.save(existing)).thenReturn(existing);
        when(orderItemMapper.toResponse(existing)).thenReturn(response);

        OrderItemResponseDto result = orderItemService.update(1L, request);

        assertEquals(5L, result.getOrderId());
        assertEquals(6L, result.getItemId());
        assertEquals(10, result.getQuantity());
        assertEquals(newOrder, existing.getOrder());
        assertEquals(newItem, existing.getItem());
    }

    @Test
    void update_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder().quantity(2).build();
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.update(1L, request));
    }

    @Test
    void update_shouldThrowException_whenOrderMissing() {
        OrderItem existing = new OrderItem();
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder().orderId(10L).build();

        when(orderItemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.update(1L, request));
    }

    @Test
    void update_shouldThrowException_whenItemMissing() {
        OrderItem existing = new OrderItem();
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder().itemId(10L).build();

        when(orderItemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> orderItemService.update(1L, request));
    }

    @Test
    void delete_shouldRemoveOrderItem_whenExists() {
        OrderItem existing = new OrderItem();

        when(orderItemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        orderItemService.delete(1L);

        verify(orderItemRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsException() {
        when(orderItemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.delete(1L));
    }
}
