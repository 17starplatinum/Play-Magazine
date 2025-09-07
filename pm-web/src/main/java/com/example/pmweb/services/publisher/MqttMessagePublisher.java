package com.example.pmweb.services.publisher;

import com.example.pmweb.exceptions.unavailable.MessagePublishingException;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
public class MqttMessagePublisher {
    private final IMqttAsyncClient mqttClient;

    public MqttMessagePublisher(IMqttAsyncClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            throw new MessagePublishingException("Failed to public message");
        }
    }
}
