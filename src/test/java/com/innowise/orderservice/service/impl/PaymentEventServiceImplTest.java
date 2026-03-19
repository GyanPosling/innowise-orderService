package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.InvalidPaymentEventException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.exception.UnsupportedPaymentEventVersionException;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(150))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-success-10")
                .orderId(10L)
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(150))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
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
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(90))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-failed-11")
                .orderId(11L)
                .userId(order.getUserId())
                .status(PaymentEventStatus.FAILED)
                .paymentAmount(BigDecimal.valueOf(90))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
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
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000013"))
                .status(OrderStatus.PAID)
                .totalPrice(BigDecimal.valueOf(75))
                .processedPaymentIds(Set.of("payment-success-13"))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-success-13")
                .orderId(13L)
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(75))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
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
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000014"))
                .status(OrderStatus.SHIPPED)
                .totalPrice(BigDecimal.valueOf(120))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-success-14")
                .orderId(14L)
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(120))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(14L)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusTransitionException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenOrderMissing() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-success-12")
                .orderId(12L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000012"))
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(100))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(12L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenPaymentIdMissing() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .orderId(15L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000015"))
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(100))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        assertThrows(InvalidPaymentEventException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenVersionUnsupported() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-version-16")
                .orderId(16L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000016"))
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(100))
                .eventVersion(99)
                .build();

        assertThrows(UnsupportedPaymentEventVersionException.class,
                () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenUserDoesNotMatchOrderOwner() {
        Order order = Order.builder()
                .id(17L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000017"))
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(55))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-user-mismatch-17")
                .orderId(17L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(55))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(17L)).thenReturn(Optional.of(order));

        assertThrows(InvalidPaymentEventException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }

    @Test
    void handlePaymentCreatedEvent_shouldThrowWhenAmountDoesNotMatchOrderTotal() {
        Order order = Order.builder()
                .id(18L)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000018"))
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(60))
                .build();
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-amount-mismatch-18")
                .orderId(18L)
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(59))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        when(orderRepository.findByIdAndDeletedAtIsNull(18L)).thenReturn(Optional.of(order));

        assertThrows(InvalidPaymentEventException.class, () -> paymentEventService.handlePaymentCreatedEvent(event));
    }
}
