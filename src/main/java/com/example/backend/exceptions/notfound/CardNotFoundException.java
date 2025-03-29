package com.example.backend.exceptions.notfound;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
