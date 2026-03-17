package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentEventServiceImpl paymentEventService;

    @Test
    void handlePaymentCreatedEvent_shouldMarkOrderPaidWhenPaymentSucceeded() {
        Order order = Order.builder()
                .id(10L)
                .status(OrderStatus.NEW)
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(10L)
                .status(PaymentEventStatus.SUCCESS)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(order));

        paymentEventService.handlePaymentCreatedEvent(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void handlePaymentCreatedEvent_shouldMarkOrderCancelledWhenPaymentFailed() {
        Order order = Order.builder()
                .id(11L)
                .status(OrderStatus.NEW)
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(11L)
                .status(PaymentEventStatus.FAILED)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(order));

        paymentEventService.handlePaymentCreatedEvent(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void handlePaymentCreatedEvent_shouldIgnoreDuplicatePaymentEvent() {
        Order order = Order.builder()
                .id(13L)
                .status(OrderStatus.PAID)
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(13L)
                .status(PaymentEventStatus.SUCCESS)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(13L)).thenReturn(Optional.of(order));

        paymentEventService.handlePaymentCreatedEvent(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenTransitionIsInvalid() {
        Order order = Order.builder()
                .id(14L)
                .status(OrderStatus.SHIPPED)
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(14L)
                .status(PaymentEventStatus.SUCCESS)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(14L)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusTransitionException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenOrderMissing() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(12L)
                .status(PaymentEventStatus.SUCCESS)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(12L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }
}
