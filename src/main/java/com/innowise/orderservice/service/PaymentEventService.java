package com.innowise.orderservice.service;

import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;

public interface PaymentEventService {

    void handlePaymentCreatedEvent(PaymentCreatedEvent event);
}
