package com.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMethodSecurity
@EnableCaching
@EnableScheduling
public class BankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankApplication.class, args);
	}
}