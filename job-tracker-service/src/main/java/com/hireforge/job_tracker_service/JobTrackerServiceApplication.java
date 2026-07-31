package com.hireforge.job_tracker_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JobTrackerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobTrackerServiceApplication.class, args);
	}

}
