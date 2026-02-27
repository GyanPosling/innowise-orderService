package com.innowise.orderservice.controller.api;

import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Orders", description = "Order management API")
public interface OrderControllerApi {

    @Operation(summary = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    ResponseEntity<OrderResponse> create(OrderCreateRequest request);

    @Operation(summary = "Get order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<OrderResponse> getById(Long id);

    @Operation(summary = "Get orders with filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved")
    })
    ResponseEntity<Page<OrderResponse>> getAll(
            Instant createdFrom,
            Instant createdTo,
            Collection<OrderStatus> statuses,
            boolean includeDeleted,
            Pageable pageable
    );

    @Operation(summary = "Get orders by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<List<OrderResponse>> getByUserId(Long userId, boolean includeDeleted);

    @Operation(summary = "Partially update order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<OrderResponse> update(
            Long id,
            OrderUpdateRequest request
    );

    @Operation(summary = "Delete order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<Void> delete(Long id);
}
