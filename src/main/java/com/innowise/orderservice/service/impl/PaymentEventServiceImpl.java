package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.PaymentEventService;
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
        Order order = orderRepository.findByIdAndDeletedAtIsNull(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));
        OrderStatus targetStatus = resolveStatus(event.getStatus());
        validateTransition(order, targetStatus, event);

        if (order.getStatus() == targetStatus) {
            log.info("Skipping duplicate payment event for orderId={}, status={}", event.getOrderId(), targetStatus);
            return;
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(targetStatus);
        log.info("Updated orderId={} status {} -> {} from payment event", event.getOrderId(), previousStatus, targetStatus);
    }

    private OrderStatus resolveStatus(PaymentEventStatus paymentStatus) {
        return paymentStatus == PaymentEventStatus.SUCCESS ? OrderStatus.PAID : OrderStatus.CANCELLED;
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
}
