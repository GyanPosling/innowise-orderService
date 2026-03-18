package com.innowise.orderservice.config;

import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic paymentCreatedTopic(
            @Value("${app.kafka.topics.payment-created}") String paymentCreatedTopic
    ) {
        return TopicBuilder.name(paymentCreatedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCreatedDlqTopic(
            @Value("${app.kafka.topics.payment-created-dlq}") String paymentCreatedDlqTopic
    ) {
        return TopicBuilder.name(paymentCreatedDlqTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, PaymentCreatedEvent> paymentEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> producerProperties = new HashMap<>();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        producerProperties.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Bean
    public KafkaTemplate<String, PaymentCreatedEvent> paymentEventKafkaTemplate(
            ProducerFactory<String, PaymentCreatedEvent> paymentEventProducerFactory
    ) {
        return new KafkaTemplate<>(paymentEventProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, PaymentCreatedEvent> paymentEventConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.innowise.orderservice.messaging.event");
        consumerProperties.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentCreatedEvent.class.getName());
        consumerProperties.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(PaymentCreatedEvent.class, false)
        );
    }

    @Bean
    public DefaultErrorHandler paymentEventKafkaErrorHandler(
            KafkaTemplate<String, PaymentCreatedEvent> paymentEventKafkaTemplate,
            @Value("${app.kafka.topics.payment-created-dlq}") String paymentCreatedDlqTopic,
            @Value("${app.kafka.consumer.payment-created.retry.initial-interval-ms:1000}") long retryInitialIntervalMs,
            @Value("${app.kafka.consumer.payment-created.retry.multiplier:2.0}") double retryMultiplier,
            @Value("${app.kafka.consumer.payment-created.retry.max-interval-ms:10000}") long retryMaxIntervalMs,
            @Value("${app.kafka.consumer.payment-created.retry.max-attempts:3}") int retryMaxAttempts
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                paymentEventKafkaTemplate,
                (record, exception) -> new TopicPartition(paymentCreatedDlqTopic, -1)
        );

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retryMaxAttempts);
        backOff.setInitialInterval(retryInitialIntervalMs);
        backOff.setMaxInterval(retryMaxIntervalMs);
        backOff.setMultiplier(retryMultiplier);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(OrderNotFoundException.class, OrderStatusTransitionException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCreatedEvent> paymentEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentCreatedEvent> consumerFactory,
            DefaultErrorHandler paymentEventKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(paymentEventKafkaErrorHandler);
        return factory;
    }
}
