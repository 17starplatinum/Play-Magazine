package com.example.pmweb.configs;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        return options;
    }

    @Bean
    public IMqttAsyncClient mqttAsyncClient() throws MqttException {
        return new MqttAsyncClient(brokerUrl, clientId);
    }
}
