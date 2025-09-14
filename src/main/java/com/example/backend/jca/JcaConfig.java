package com.example.backend.jca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.resource.ResourceException;

@Configuration
public class JcaConfig {

    @Bean
    public BitrixManagedConnectionFactory bitrixManagedConnectionFactory() {
        BitrixManagedConnectionFactory factory = new BitrixManagedConnectionFactory();
        factory.setBaseUrl("https://b24-yc4n1w.bitrix24.ru");
        factory.setUserId("1");
        return factory;
    }

    @Bean
    public BitrixConnectionFactory bitrixConnectionFactory(BitrixManagedConnectionFactory mcf) throws ResourceException {
        return (BitrixConnectionFactory) mcf.createConnectionFactory();
    }
}