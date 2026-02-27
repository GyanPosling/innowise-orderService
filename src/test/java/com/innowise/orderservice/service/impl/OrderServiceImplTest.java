package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.orderservice.config.security.SecurityUtil;
import com.innowise.orderservice.exception.BadRequestException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.ServiceUnavailableException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.request.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.dto.response.UserInfoResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.UserLookupService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void create_shouldThrowBadRequest_whenUserEmailMissing() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .userId(1L)
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequest.builder().itemId(1L).quantity(1).build()))
                .build();

        assertThrows(BadRequestException.class, () -> orderService.create(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_shouldUseTokenUser_whenNotAdmin() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .userId(1L)
                .userEmail("request@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequest.builder().itemId(1L).quantity(1).build()))
                .build();
        Order order = new Order();
        OrderResponse response = OrderResponse.builder()
                .id(10L)
                .userId(5L)
                .userEmail("current@example.com")
                .build();
        UserInfoResponse userInfo = UserInfoResponse.builder().email("current@example.com").build();
        Item item = new Item();
        item.setId(1L);

        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getCurrentUserId()).thenReturn(5L);
        when(securityUtil.getCurrentUsername()).thenReturn("current@example.com");
        when(itemRepository.findAllByIdInAndDeletedAtIsNull(any())).thenReturn(List.of(item));
        when(orderMapper.toEntity(eq(request), anyMap())).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(userLookupService.getUserByEmail("current@example.com")).thenReturn(userInfo);

        OrderResponse result = orderService.create(request);

        assertEquals(5L, request.getUserId());
        assertEquals("current@example.com", request.getUserEmail());
        assertNotNull(result.getUser());
        assertEquals("current@example.com", result.getUser().getEmail());
    }

    @Test
    void create_shouldThrowBadRequest_whenTokenUserIdMissing() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequest.builder().itemId(1L).quantity(1).build()))
                .build();

        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getCurrentUserId()).thenReturn(null);

        assertThrows(BadRequestException.class, () -> orderService.create(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_shouldThrowItemNotFound_whenSomeItemsMissing() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(
                        OrderItemRequest.builder().itemId(1L).quantity(1).build(),
                        OrderItemRequest.builder().itemId(2L).quantity(1).build()))
                .build();
        Item item = new Item();
        item.setId(1L);

        when(securityUtil.isAdmin()).thenReturn(true);
        when(itemRepository.findAllByIdInAndDeletedAtIsNull(anyCollection())).thenReturn(List.of(item));

        assertThrows(ItemNotFoundException.class, () -> orderService.create(request));
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getById(1L));
    }

    @Test
    void getById_shouldEnrichUserInfo() {
        Order order = new Order();
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userEmail("user@example.com")
                .build();
        UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(userLookupService.getUserByEmail("user@example.com")).thenReturn(userInfo);

        OrderResponse result = orderService.getById(1L);

        assertNotNull(result.getUser());
        assertEquals("user@example.com", result.getUser().getEmail());
    }

    @Test
    void getById_shouldSkipUserFetch_whenEmailMissing() {
        Order order = new Order();
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userEmail(" ")
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getById(1L);

        assertEquals(" ", result.getUserEmail());
        verify(userLookupService, never()).getUserByEmail(anyString());
    }

    @Test
    void getAll_shouldEnrichUserInfo() {
        Order order = new Order();
        order.setUserEmail("user@example.com");
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userEmail("user@example.com")
                .build();
        UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(userLookupService.getUsersByEmails(anyList())).thenReturn(List.of(userInfo));

        OrderResponse result = orderService.getAll(Instant.now(), Instant.now(), List.of(OrderStatus.NEW), false, pageable)
                .getContent()
                .get(0);

        assertNotNull(result.getUser());
        assertEquals("user@example.com", result.getUser().getEmail());
    }

    @Test
    void getByUserId_shouldReturnOrders() {
        Order order = new Order();
        order.setUserEmail("user@example.com");
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userEmail("user@example.com")
                .build();
        UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();

        when(orderRepository.findAllByUserIdAndDeletedAtIsNull(7L)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(userLookupService.getUsersByEmails(anyList())).thenReturn(List.of(userInfo));

        List<OrderResponse> results = orderService.getByUserId(7L, false);

        assertEquals(1, results.size());
        assertEquals("user@example.com", results.get(0).getUser().getEmail());
    }

    @Test
    void update_shouldThrowBadRequest_whenUserEmailBlank() {
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .userEmail("")
                .build();
        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(new Order()));

        assertThrows(BadRequestException.class, () -> orderService.update(1L, request));
    }

    @Test
    void update_shouldSetUserId_whenAdmin() {
        Order existing = new Order();
        existing.setUserId(1L);
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .userId(99L)
                .build();
        OrderResponse response = OrderResponse.builder().id(1L).userId(99L).build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(securityUtil.isAdmin()).thenReturn(true);
        when(orderRepository.save(existing)).thenReturn(existing);
        when(orderMapper.toResponse(existing)).thenReturn(response);

        OrderResponse result = orderService.update(1L, request);

        assertEquals(99L, existing.getUserId());
        assertEquals(99L, result.getUserId());
    }

    @Test
    void update_shouldIgnoreUserId_whenNotAdmin() {
        Order existing = new Order();
        existing.setUserId(1L);
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .userId(99L)
                .build();
        OrderResponse response = OrderResponse.builder().id(1L).userId(1L).build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(securityUtil.isAdmin()).thenReturn(false);
        when(orderRepository.save(existing)).thenReturn(existing);
        when(orderMapper.toResponse(existing)).thenReturn(response);

        OrderResponse result = orderService.update(1L, request);

        assertEquals(1L, existing.getUserId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void update_shouldApplyChanges_whenFieldsProvided() {
        Order existing = new Order();
        existing.setOrderItems(new ArrayList<>());
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .status(OrderStatus.PAID)
                .totalPrice(BigDecimal.valueOf(99))
                .items(List.of(OrderItemRequest.builder().itemId(1L).quantity(2).build()))
                .build();
        OrderItem item = new OrderItem();
        OrderResponse response = OrderResponse.builder().id(1L).status(OrderStatus.PAID).build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findAllByIdInAndDeletedAtIsNull(anyCollection())).thenReturn(List.of(Item.builder().id(1L).build()));
        when(orderMapper.toOrderItems(eq(existing), eq(request.getItems()), anyMap())).thenReturn(List.of(item));
        when(orderRepository.save(existing)).thenReturn(existing);
        when(orderMapper.toResponse(existing)).thenReturn(response);

        OrderResponse result = orderService.update(1L, request);

        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals(1, existing.getOrderItems().size());
    }

    @Test
    void update_shouldCreateItemsList_whenMissing() {
        Order existing = new Order();
        existing.setOrderItems(null);
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .items(List.of(OrderItemRequest.builder().itemId(1L).quantity(1).build()))
                .build();
        OrderItem orderItem = new OrderItem();
        OrderResponse response = OrderResponse.builder().id(1L).build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findAllByIdInAndDeletedAtIsNull(anyCollection())).thenReturn(List.of(Item.builder().id(1L).build()));
        when(orderMapper.toOrderItems(eq(existing), eq(request.getItems()), anyMap())).thenReturn(List.of(orderItem));
        when(orderRepository.save(existing)).thenReturn(existing);
        when(orderMapper.toResponse(existing)).thenReturn(response);

        orderService.update(1L, request);

        assertNotNull(existing.getOrderItems());
        assertEquals(1, existing.getOrderItems().size());
    }

    @Test
    void getById_shouldReturnResponse_whenUserServiceFails() {
        Order order = new Order();
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userEmail("user@example.com")
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(userLookupService.getUserByEmail("user@example.com"))
                .thenThrow(new ServiceUnavailableException("User service is unavailable", new RuntimeException()));

        assertThrows(ServiceUnavailableException.class, () -> orderService.getById(1L));
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(orderRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.delete(1L));
    }

    @Test
    void delete_shouldRemoveOrder_whenExists() {
        Order order = new Order();
        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));

        orderService.delete(1L);

        verify(orderRepository).delete(order);
    }
}
