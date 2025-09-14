package com.example.pmcore.exceptions.notfound;

public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException(String message) {
        super(message);
    }
}
