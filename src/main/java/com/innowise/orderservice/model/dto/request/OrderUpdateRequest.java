package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class OrderUpdateRequest {

    private UUID userId;

    @Email
    @Pattern(regexp = "^\\S.*", message = "must not be blank")
    @Size(max = 100)
    private String userEmail;

    private OrderStatus status;

    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal totalPrice;

    @Valid
    @Size(min = 1)
    private List<OrderItemRequest> items;
}
