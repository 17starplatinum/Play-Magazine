package com.example.pmcore.exceptions.accepted;

public class RequestPendingException extends RuntimeException {
    public RequestPendingException(String message) {
        super(message);
    }
}
