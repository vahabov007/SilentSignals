package com.vahabvahabov.SilentSignals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.vahabvahabov.SilentSignals")
@EntityScan(basePackages = "com.vahabvahabov.SilentSignals")
@EnableJpaRepositories(basePackages = "com.vahabvahabov.SilentSignals")
public class SilentSignalsStarter {

	public static void main(String[] args) {
		SpringApplication.run(SilentSignalsStarter.class, args);
	}
}

