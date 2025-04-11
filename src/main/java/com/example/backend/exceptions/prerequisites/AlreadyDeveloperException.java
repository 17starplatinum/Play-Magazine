package com.example.backend.exceptions.prerequisites;

public class AlreadyDeveloperException extends RuntimeException {
    public AlreadyDeveloperException(String message, Throwable cause) {
        super(message, cause);
    }
}
