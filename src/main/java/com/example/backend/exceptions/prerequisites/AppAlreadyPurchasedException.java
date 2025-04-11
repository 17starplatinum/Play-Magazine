package com.example.backend.exceptions.prerequisites;

public class AppAlreadyPurchasedException extends RuntimeException {
    public AppAlreadyPurchasedException(String message, Throwable cause) {
        super(message, cause);
    }
}
