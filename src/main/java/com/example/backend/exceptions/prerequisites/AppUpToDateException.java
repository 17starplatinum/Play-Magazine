package com.example.backend.exceptions.prerequisites;

public class AppUpToDateException extends RuntimeException {
    public AppUpToDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
