package com.example.pmweb.exceptions.badcredentials;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
