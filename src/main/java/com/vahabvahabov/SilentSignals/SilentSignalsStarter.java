package com.vahabvahabov.SilentSignals;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages = "com.vahabvahabov.SilentSignals")
@EntityScan(basePackages = "com.vahabvahabov.SilentSignals")
@EnableJpaRepositories(basePackages = "com.vahabvahabov.SilentSignals")
public class SilentSignalsStarter {

	public static void main(String[] args) {
		SpringApplication.run(SilentSignalsStarter.class, args);
	}
}

@Component
class DatabaseSchemaInitializer implements CommandLineRunner {

	private final JdbcTemplate jdbcTemplate;

	public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(String... args) throws Exception {
		String schemaSql = "CREATE SCHEMA IF NOT EXISTS silent_signals";
		jdbcTemplate.execute(schemaSql);
		System.out.println("Schema 'silent_signals' created successfully.");
	}
}