package com.example.pmcore.exceptions.badcredentials;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
