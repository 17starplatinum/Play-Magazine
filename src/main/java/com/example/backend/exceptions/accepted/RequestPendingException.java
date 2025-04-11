package com.example.backend.exceptions.accepted;

public class RequestPendingException extends RuntimeException {
    public RequestPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
