package com.innowise.orderservice.integration;

import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentEventStatus;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentEventConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.payment-created}")
    private String paymentCreatedTopic;

    @Value("${app.kafka.topics.payment-created-dlq}")
    private String paymentCreatedDlqTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build()).get();

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.PAID);
        assertThat(countProcessedPaymentIds(order.getId())).isEqualTo(1L);
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
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build()).get();

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.CANCELLED);
        assertThat(countProcessedPaymentIds(order.getId())).isEqualTo(1L);
    }

    @Test
    void handlePaymentCreatedEvent_shouldIgnoreDuplicatePaymentId() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000032"))
                .userEmail("buyer32@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(75))
                .build());

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-duplicate-1")
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(75))
                .timestamp(Instant.parse("2026-03-16T10:25:30Z"))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), event).get();
        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), event).get();

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.PAID);
        assertThat(countProcessedPaymentIds(order.getId())).isEqualTo(1L);
    }

    @Test
    void handlePaymentCreatedEvent_shouldRouteInvalidTransitionToDlq() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000033"))
                .userEmail("buyer33@example.com")
                .status(OrderStatus.SHIPPED)
                .totalPrice(BigDecimal.valueOf(200))
                .build());

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-invalid-transition-1")
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(PaymentEventStatus.FAILED)
                .paymentAmount(BigDecimal.valueOf(200))
                .timestamp(Instant.parse("2026-03-16T10:30:30Z"))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), event).get();

        try (Consumer<String, PaymentCreatedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(paymentCreatedDlqTopic));
            ConsumerRecord<String, PaymentCreatedEvent> dlqRecord = pollForRecord(consumer, event.getPaymentId());

            assertThat(dlqRecord).isNotNull();
            assertThat(dlqRecord.value().getPaymentId()).isEqualTo(event.getPaymentId());
            assertThat(dlqRecord.value().getOrderId()).isEqualTo(order.getId());
        }

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.SHIPPED);
        assertThat(countProcessedPaymentIds(order.getId())).isZero();
    }

    @Test
    void handlePaymentCreatedEvent_shouldRouteForeignOrderPaymentToDlq() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000034"))
                .userEmail("buyer34@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(110))
                .build());

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-foreign-order-1")
                .orderId(order.getId())
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
                .status(PaymentEventStatus.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(110))
                .timestamp(Instant.parse("2026-03-16T10:35:30Z"))
                .eventVersion(PaymentCreatedEvent.CURRENT_VERSION)
                .build();

        kafkaTemplate.send(paymentCreatedTopic, String.valueOf(order.getId()), event).get();

        try (Consumer<String, PaymentCreatedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(paymentCreatedDlqTopic));
            ConsumerRecord<String, PaymentCreatedEvent> dlqRecord = pollForRecord(consumer, event.getPaymentId());

            assertThat(dlqRecord).isNotNull();
            assertThat(dlqRecord.value().getPaymentId()).isEqualTo(event.getPaymentId());
        }

        assertThat(awaitStatus(order.getId())).isEqualTo(OrderStatus.NEW);
        assertThat(countProcessedPaymentIds(order.getId())).isZero();
    }

    private OrderStatus awaitStatus(Long orderId) {
        Instant deadline = Instant.now().plusSeconds(10);
        while (Instant.now().isBefore(deadline)) {
            OrderStatus currentStatus = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                    .map(Order::getStatus)
                    .orElse(null);
            if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.CANCELLED) {
                return currentStatus;
            }
        }
        return orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .map(Order::getStatus)
                .orElse(null);
    }

    private long countProcessedPaymentIds(Long orderId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_payment_events WHERE order_id = ?",
                Long.class,
                orderId
        );
        return count != null ? count : 0L;
    }

    private Consumer<String, PaymentCreatedEvent> createConsumer() {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "order-it-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.innowise.orderservice.messaging.event",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentCreatedEvent.class.getName()
        );
        return new KafkaConsumer<>(properties);
    }

    private ConsumerRecord<String, PaymentCreatedEvent> pollForRecord(
            Consumer<String, PaymentCreatedEvent> consumer,
            String paymentId
    ) {
        Instant deadline = Instant.now().plusSeconds(10);
        while (Instant.now().isBefore(deadline)) {
            ConsumerRecords<String, PaymentCreatedEvent> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, PaymentCreatedEvent> consumerRecord : records) {
                if (consumerRecord.value() != null
                        && paymentId.equals(consumerRecord.value().getPaymentId())) {
                    return consumerRecord;
                }
            }
        }
        return null;
    }
}
