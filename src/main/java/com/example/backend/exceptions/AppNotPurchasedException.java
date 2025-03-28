package com.example.backend.exceptions;

public class AppNotPurchasedException extends RuntimeException {
    public AppNotPurchasedException(String message, Throwable cause) {
        super(message, cause);
    }
}
