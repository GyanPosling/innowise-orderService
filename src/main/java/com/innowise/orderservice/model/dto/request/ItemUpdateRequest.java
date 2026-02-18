package com.innowise.orderservice.model.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ItemUpdateRequest {

    private String name;

    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal price;
}
