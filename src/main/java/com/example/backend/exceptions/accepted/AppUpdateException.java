package com.example.backend.exceptions.accepted;

public class AppUpdateException extends RuntimeException {
    public AppUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
