package com.example.backend.exceptions;

public class AppAlreadyPurchasedException extends RuntimeException {
    public AppAlreadyPurchasedException(String message, Throwable cause) {
        super(message, cause);
    }
}
