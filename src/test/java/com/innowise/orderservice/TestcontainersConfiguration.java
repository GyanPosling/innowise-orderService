package com.innowise.orderservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

	@TestConfiguration(proxyBeanMethods = false)
	public class TestcontainersConfiguration {

		private static final KafkaContainer KAFKA_CONTAINER =
				new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

	static {
		KAFKA_CONTAINER.start();
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
	}

	@Bean
	KafkaContainer kafkaContainer() {
		return KAFKA_CONTAINER;
	}

	public static KafkaContainer getKafkaContainer() {
		return KAFKA_CONTAINER;
	}

}
