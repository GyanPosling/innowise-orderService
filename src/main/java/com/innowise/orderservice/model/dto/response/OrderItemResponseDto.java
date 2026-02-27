package com.innowise.orderservice.model.dto.response;

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
public class OrderItemResponseDto {

    private Long id;

    private Long orderId;

    private Long itemId;

    private Integer quantity;
}
