package com.example.backend.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BitrixApiConfig {
    @Value("bitrix24.api.allcards")
    private String allCardsToken;
}
