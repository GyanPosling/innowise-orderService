package com.innowise.orderservice.controller.api;

import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order Items", description = "Order item management API")
public interface OrderItemControllerApi {

    @Operation(summary = "Create a new order item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order item created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderItemResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    ResponseEntity<OrderItemResponseDto> create(Long orderId, OrderItemCreateRequest request);

    @Operation(summary = "Get order item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderItemResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    ResponseEntity<OrderItemResponseDto> getById(Long orderId, Long itemId);

    @Operation(summary = "Get all order items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order items retrieved")
    })
    ResponseEntity<List<OrderItemResponseDto>> getAll(Long orderId);

    @Operation(summary = "Partially update order item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderItemResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    ResponseEntity<OrderItemResponseDto> update(Long orderId, Long itemId, OrderItemUpdateRequest request);

    @Operation(summary = "Delete order item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order item deleted"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    ResponseEntity<Void> delete(Long orderId, Long itemId);
}
