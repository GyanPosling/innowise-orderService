package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
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
public class OrderCreateRequest {

    @Positive
    private Long userId;

    @NotBlank
    @Email
    @Size(max = 100)
    private String userEmail;

    @NotNull
    private OrderStatus status;

    @NotNull
    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<OrderItemRequest> items;
}
