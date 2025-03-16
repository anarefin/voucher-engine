package com.bracit.voucher_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main application class for the Voucher Engine
 * Configured to use virtual threads for improved performance and scalability
 */
@SpringBootApplication
@EnableAsync
@RestController
public class VoucherEngineApplication {

	public static void main(String[] args) {
		// Enable virtual threads for the JVM
		System.setProperty("spring.threads.virtual.enabled", "true");
		SpringApplication.run(VoucherEngineApplication.class, args);
	}

	@GetMapping("/")
	public String hello() {
		return "Hello World";
	}

}
