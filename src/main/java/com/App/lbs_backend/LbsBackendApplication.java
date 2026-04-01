package com.App.lbs_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class LbsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LbsBackendApplication.class, args);
	}

}
