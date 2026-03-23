package com.innowise.orderservice.model.dto.response;

import com.innowise.orderservice.model.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
public class OrderResponse {

    private Long id;

    private UUID userId;

    private String userEmail;

    private UserInfoResponse user;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private Instant deletedAt;

    private Instant createdAt;

    private Instant updatedAt;

    private List<OrderItemResponse> items;
}
