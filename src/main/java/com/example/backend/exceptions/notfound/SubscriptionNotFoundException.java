package com.example.backend.exceptions.notfound;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
