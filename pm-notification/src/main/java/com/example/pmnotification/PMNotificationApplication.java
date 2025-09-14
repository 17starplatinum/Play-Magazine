package com.example.pmnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class PMNotificationApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PMNotificationApplication.class, args);
    }
}
