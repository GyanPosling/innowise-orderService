package com.innowise.orderservice.controller;

import com.innowise.orderservice.controller.api.OrderItemControllerApi;
import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.service.OrderItemService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController implements OrderItemControllerApi {

    private final OrderItemService orderItemService;

    @PostMapping
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#orderId)")
    public ResponseEntity<OrderItemResponseDto> create(@PathVariable Long orderId,
                                                       @Valid @RequestBody OrderItemCreateRequest request) {
        OrderItemResponseDto response = orderItemService.create(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{itemId}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#orderId)")
    public ResponseEntity<OrderItemResponseDto> getById(@PathVariable Long orderId,
                                                        @PathVariable Long itemId) {
        return ResponseEntity.ok(orderItemService.getById(orderId, itemId));
    }

    @GetMapping
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#orderId)")
    public ResponseEntity<List<OrderItemResponseDto>> getAll(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.getAll(orderId));
    }

    @PatchMapping("/{itemId}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#orderId)")
    public ResponseEntity<OrderItemResponseDto> update(@PathVariable Long orderId,
                                                       @PathVariable Long itemId,
                                                       @Valid @RequestBody OrderItemUpdateRequest request) {
        return ResponseEntity.ok(orderItemService.update(orderId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#orderId)")
    public ResponseEntity<Void> delete(@PathVariable Long orderId, @PathVariable Long itemId) {
        orderItemService.delete(orderId, itemId);
        return ResponseEntity.noContent().build();
    }
}
