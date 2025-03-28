package com.example.backend.exceptions;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
