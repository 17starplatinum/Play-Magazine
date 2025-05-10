package com.example.backend.exceptions.conflict;

public class UnspecifiedCardException extends RuntimeException {
    public UnspecifiedCardException(String message) {
        super(message);
    }
}
