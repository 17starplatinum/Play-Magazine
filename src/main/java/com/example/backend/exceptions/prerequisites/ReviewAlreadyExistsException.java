package com.example.backend.exceptions.prerequisites;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
