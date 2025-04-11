package com.example.backend.exceptions.notfound;

public class AppNotFoundException extends RuntimeException {
    public AppNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
