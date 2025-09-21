package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
@SpringBootApplication
@EntityScan(basePackages = "com.example.demo.entity")
public class Demo011Application {

	public static void main(String[] args) {
		SpringApplication.run(Demo011Application.class, args);
	}

}
