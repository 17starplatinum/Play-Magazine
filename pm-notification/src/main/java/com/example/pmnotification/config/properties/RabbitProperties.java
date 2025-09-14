package com.example.pmnotification.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@Getter
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitProperties {
    private String host = "localhost";
    private int port = 5672;
    private String username = "rmuser";
    private String password = "rmpassword";
    private String virtualHost = "/";
    private String queueName = "email-notification";
    private String exchangeName = "email.exchange";
}