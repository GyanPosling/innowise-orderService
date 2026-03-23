package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.InvalidPaymentEventException;
import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UnsupportedPaymentEventVersionException;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.PaymentEventService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventServiceImpl implements PaymentEventService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void handlePaymentCreatedEvent(PaymentCreatedEvent event) {
        validateEvent(event);
        Order order = orderRepository.findByIdAndDeletedAtIsNull(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));
        validateOrderCompatibility(order, event);
        if (order.getProcessedPaymentIds().contains(event.getPaymentId())) {
            log.info("Skipping duplicate payment event by paymentId={} for orderId={}",
                    event.getPaymentId(), event.getOrderId());
            return;
        }

        OrderStatus targetStatus = resolveStatus(event.getStatus());
        validateTransition(order, targetStatus, event);

        if (order.getStatus() == targetStatus) {
            order.getProcessedPaymentIds().add(event.getPaymentId());
            log.info("Skipping duplicate payment event by target status for paymentId={}, orderId={}, status={}",
                    event.getPaymentId(), event.getOrderId(), targetStatus);
            return;
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(targetStatus);
        order.getProcessedPaymentIds().add(event.getPaymentId());
        log.info("Updated orderId={} status {} -> {} from payment event", event.getOrderId(), previousStatus, targetStatus);
    }

    private OrderStatus resolveStatus(PaymentEventStatus paymentStatus) {
        return paymentStatus == PaymentEventStatus.SUCCESS ? OrderStatus.PAID : OrderStatus.CANCELLED;
    }

    private void validateEvent(PaymentCreatedEvent event) {
        if (event.getOrderId() == null) {
            throw new InvalidPaymentEventException("Payment event must contain orderId");
        }
        if (event.getStatus() == null) {
            throw new InvalidPaymentEventException("Payment event must contain status");
        }
        if (event.getPaymentId() == null || event.getPaymentId().isBlank()) {
            throw new InvalidPaymentEventException("Payment event must contain paymentId for idempotency");
        }
        if (event.getUserId() == null) {
            throw new InvalidPaymentEventException("Payment event must contain userId");
        }
        if (event.getPaymentAmount() == null) {
            throw new InvalidPaymentEventException("Payment event must contain paymentAmount");
        }
        if (event.getEventVersion() != null && event.getEventVersion() != PaymentCreatedEvent.CURRENT_VERSION) {
            throw new UnsupportedPaymentEventVersionException(
                    "Unsupported payment event version: " + event.getEventVersion()
            );
        }
    }

    private void validateTransition(Order order, OrderStatus targetStatus, PaymentCreatedEvent event) {
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus.canTransitionTo(targetStatus)) {
            return;
        }
        throw new OrderStatusTransitionException(
                "Invalid payment-driven status transition for orderId=%d: %s -> %s".formatted(
                        event.getOrderId(),
                        currentStatus,
                        targetStatus
                )
        );
    }

    private void validateOrderCompatibility(Order order, PaymentCreatedEvent event) {
        if (order.getUserId() == null || !order.getUserId().equals(event.getUserId())) {
            throw new InvalidPaymentEventException(
                    "Payment event user does not match order owner for orderId=" + event.getOrderId()
            );
        }
        BigDecimal totalPrice = order.getTotalPrice();
        if (totalPrice == null || event.getPaymentAmount().compareTo(totalPrice) != 0) {
            throw new InvalidPaymentEventException(
                    "Payment amount does not match order total for orderId=" + event.getOrderId()
            );
        }
    }
}
