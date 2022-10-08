package com.drive.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DriveIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriveIntegrationApplication.class, args);
	}

}
