package com.innowise.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OrderserviceApplicationTests {

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add(
				"spring.kafka.bootstrap-servers",
				() -> TestcontainersConfiguration.getKafkaContainer().getBootstrapServers()
		);
	}

	@Test
	void contextLoads() {
	}

}
