package com.example.backend.exceptions;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
