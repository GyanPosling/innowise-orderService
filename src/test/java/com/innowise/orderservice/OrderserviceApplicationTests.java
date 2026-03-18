package com.innowise.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
		partitions = 1,
		topics = {"create-payment", "create-payment.dlq"},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
