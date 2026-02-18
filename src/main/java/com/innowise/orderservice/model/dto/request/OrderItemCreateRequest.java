package com.innowise.orderservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderItemCreateRequest {

    @NotNull
    @Positive
    private Long orderId;

    @NotNull
    @Positive
    private Long itemId;

    @NotNull
    @Positive
    private Integer quantity;
}
