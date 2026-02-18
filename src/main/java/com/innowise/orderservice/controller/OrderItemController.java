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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController implements OrderItemControllerApi {

    private final OrderItemService orderItemService;

    @PostMapping
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderId(#request.orderId)")
    public ResponseEntity<OrderItemResponseDto> create(@Valid @RequestBody OrderItemCreateRequest request) {
        OrderItemResponseDto response = orderItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderItemId(#id)")
    public ResponseEntity<OrderItemResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getById(id));
    }

    @GetMapping
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderItemResponseDto>> getAll() {
        return ResponseEntity.ok(orderItemService.getAll());
    }

    @PutMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderItemId(#id)")
    public ResponseEntity<OrderItemResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody OrderItemUpdateRequest request) {
        return ResponseEntity.ok(orderItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isOwnerByOrderItemId(#id)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
