package com.innowise.orderservice.service.impl;

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
import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.config.security.SecurityUtil;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.specification.OrderSpecifications;
import com.innowise.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        if (request.getUserEmail() == null || request.getUserEmail().isBlank()) {
            throw new BadRequestException("User email is required");
        }
        if (!securityUtil.isAdmin()) {
            Long currentUserId = securityUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BadRequestException("User id is missing in token");
            }
            request.setUserId(currentUserId);
            String username = securityUtil.getCurrentUsername();
            if (username != null && !username.isBlank()) {
                request.setUserEmail(username);
            }
        }
        Map<Long, Item> itemsById = resolveItems(request.getItems());
        Order order = orderMapper.toEntity(request, itemsById);
        return enrichUser(orderMapper.toResponse(orderRepository.save(order)));
    }

    @Override
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return enrichUser(orderMapper.toResponse(order));
    }

    @Override
    public Page<OrderResponse> getAll(Instant createdFrom, Instant createdTo, Collection<OrderStatus> statuses, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecifications.createdAtBetween(createdFrom, createdTo))
                .and(OrderSpecifications.statusIn(statuses));
        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toResponse)
                .map(this::enrichUser);
    }

    @Override
    public List<OrderResponse> getByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .map(this::enrichUser)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (request.getUserId() != null && securityUtil.isAdmin()) {
            order.setUserId(request.getUserId());
        }
        if (request.getUserEmail() != null && request.getUserEmail().isBlank()) {
            throw new BadRequestException("User email must not be blank");
        }
        if (request.getUserEmail() != null) {
            order.setUserEmail(request.getUserEmail());
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        }
        if (request.getItems() != null) {
            List<OrderItem> orderItems = order.getOrderItems();
            if (orderItems == null) {
                orderItems = new ArrayList<>();
                order.setOrderItems(orderItems);
            } else {
                orderItems.clear();
            }
            Map<Long, Item> itemsById = resolveItems(request.getItems());
            orderItems.addAll(orderMapper.toOrderItems(order, request.getItems(), itemsById));
        }
        return enrichUser(orderMapper.toResponse(orderRepository.save(order)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        orderRepository.delete(order);
    }

    private OrderResponse enrichUser(OrderResponse response) {
        if (response == null) {
            return null;
        }
        String email = response.getUserEmail();
        if (email == null || email.isBlank()) {
            return response;
        }
        response.setUser(fetchUser(email));
        return response;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    private UserInfoResponse fetchUser(String email) {
        return userServiceClient.getUserByEmail(email);
    }

    private UserInfoResponse getUserByEmailFallback(String email, Throwable ex) {
        throw new ServiceUnavailableException("User service is unavailable for email: " + email, ex);
    }

    private Map<Long, Item> resolveItems(List<OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return Map.of();
        }
        Set<Long> itemIds = itemRequests.stream()
                .map(OrderItemRequest::getItemId)
                .collect(Collectors.toSet());
        Map<Long, Item> itemsById = itemRepository.findAllByIdInAndDeletedAtIsNull(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
        if (itemsById.size() != itemIds.size()) {
            Long missingId = itemIds.stream()
                    .filter(id -> !itemsById.containsKey(id))
                    .findFirst()
                    .orElse(null);
            if (missingId != null) {
                throw new ItemNotFoundException(missingId);
            }
        }
        return itemsById;
    }
}
