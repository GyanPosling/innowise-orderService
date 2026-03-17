package com.innowise.orderservice.integration;

import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentEventConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.payment-created}")
    private String paymentCreatedTopic;

    @Test
    void handlePaymentCreatedEvent_shouldMarkOrderPaid() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000030"))
                .userEmail("buyer30@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(150))
                .build());

        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), PaymentCreatedEvent.builder()
                .paymentId("payment-success-1")
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(150))
                .timestamp(Instant.parse("2026-03-16T10:15:30Z"))
                .build()).get();

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void handlePaymentCreatedEvent_shouldMarkOrderCancelled() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000031"))
                .userEmail("buyer31@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(90))
                .build());

        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), PaymentCreatedEvent.builder()
                .paymentId("payment-failed-1")
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(PaymentEventStatus.FAILED)
                .paymentAmount(BigDecimal.valueOf(90))
                .timestamp(Instant.parse("2026-03-16T10:20:30Z"))
                .build()).get();

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.CANCELLED);
    }

    private OrderStatus awaitStatus(Long orderId) throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(10);
        while (Instant.now().isBefore(deadline)) {
            OrderStatus currentStatus = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                    .map(Order::getStatus)
                    .orElse(null);
            if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.CANCELLED) {
                return currentStatus;
            }
            Thread.sleep(200);
        }
        return orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .map(Order::getStatus)
                .orElse(null);
    }
}
