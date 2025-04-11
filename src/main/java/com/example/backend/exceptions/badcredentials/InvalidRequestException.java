package com.example.backend.exceptions.badcredentials;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
