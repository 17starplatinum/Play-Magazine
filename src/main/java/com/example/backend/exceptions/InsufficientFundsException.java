package com.example.backend.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
