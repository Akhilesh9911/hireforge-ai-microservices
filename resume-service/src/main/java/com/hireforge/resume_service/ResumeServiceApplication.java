package com.hireforge.resume_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ResumeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeServiceApplication.class, args);
	}

}
