package com.example.backend.exceptions.unavailable;

public class MessagePublishingException extends RuntimeException {
    public MessagePublishingException(String message) {
        super(message);
    }
}
