package com.example.backend.exceptions.prerequisites;

public class InvalidApplicationConfigException extends RuntimeException {
    public InvalidApplicationConfigException(String message) {
        super(message);
    }
}
