package com.example.backend.exceptions;

public class CardDeletedException extends RuntimeException {
    public CardDeletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
