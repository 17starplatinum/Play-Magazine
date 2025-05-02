package com.example.backend.exceptions.prerequisites;

public class AppUpToDateException extends RuntimeException {
    public AppUpToDateException(String message) {
        super(message);
    }
}
