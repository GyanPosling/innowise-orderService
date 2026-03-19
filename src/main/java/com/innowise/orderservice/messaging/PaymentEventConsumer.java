package com.innowise.orderservice.messaging;

import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.service.PaymentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentEventService paymentEventService;

    @KafkaListener(
            topics = "${app.kafka.topics.payment-created}",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void handlePaymentCreatedEvent(PaymentCreatedEvent event) {
        log.info("Received payment event for paymentId={}, orderId={}, status={}, version={}",
                event.getPaymentId(), event.getOrderId(), event.getStatus(), event.getEventVersion());
        paymentEventService.handlePaymentCreatedEvent(event);
    }
}
