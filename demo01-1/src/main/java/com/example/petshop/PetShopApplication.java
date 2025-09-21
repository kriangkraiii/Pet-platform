package com.example.petshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetShopApplication.class, args);
    }
}
