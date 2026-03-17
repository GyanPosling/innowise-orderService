package com.innowise.orderservice.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
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
public class PaymentCreatedEvent {

    private String paymentId;

    private Long orderId;

    private UUID userId;

    private PaymentEventStatus status;

    private BigDecimal paymentAmount;

    private Instant timestamp;
}
