package com.innowise.orderservice;

import org.springframework.boot.SpringApplication;

public class TestOrderserviceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderserviceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
