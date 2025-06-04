package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class PlayMagazineApplication {
    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.configure().load();
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(PlayMagazineApplication.class, args);
    }
}
