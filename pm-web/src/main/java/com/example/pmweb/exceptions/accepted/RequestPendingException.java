package com.example.pmweb.exceptions.accepted;

public class RequestPendingException extends RuntimeException {
    public RequestPendingException(String message) {
        super(message);
    }
}
