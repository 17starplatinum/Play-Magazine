package com.example.pmcommon.mappers;

import com.example.pmcommon.dto.async.PurchaseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;

public class MessageMapper {
    public static String toJson(Message message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert PurchaseMessage to JSON", e);
        }
    }

    public static PurchaseMessage fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, PurchaseMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to PurchaseMessage", e);
        }
    }
}
