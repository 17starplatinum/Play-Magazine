package com.example.backend.exceptions;

public class AppNotFoundException extends RuntimeException {
    public AppNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
