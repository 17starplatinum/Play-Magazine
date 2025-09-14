package com.example.pmcore.exceptions.unavailable;

public class MessagePublishingException extends RuntimeException {
    public MessagePublishingException(String message) {
        super(message);
    }
}
