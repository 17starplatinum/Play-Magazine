package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class PlayMagazineApplication {
    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.configure().load();
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(PlayMagazineApplication.class, args);
    }

    @Bean
    public RestClient restClient(){
        return RestClient.create();
    }

    @Bean
    public WebClient webClient(){
        return WebClient.create();
    }
}
