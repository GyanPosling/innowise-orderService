package com.innowise.orderservice.controller.api;

import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Items", description = "Item management API")
public interface ItemControllerApi {

    @Operation(summary = "Create a new item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    ResponseEntity<ItemResponse> create(ItemCreateRequest request);

    @Operation(summary = "Get item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    ResponseEntity<ItemResponse> getById(Long id);

    @Operation(summary = "Get all items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items retrieved")
    })
    ResponseEntity<List<ItemResponse>> getAll();

    @Operation(summary = "Update item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    ResponseEntity<ItemResponse> update(Long id, ItemUpdateRequest request);

    @Operation(summary = "Delete item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    ResponseEntity<Void> delete(Long id);
}
