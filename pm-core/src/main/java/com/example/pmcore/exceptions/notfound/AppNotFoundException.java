package com.example.pmcore.exceptions.notfound;

public class AppNotFoundException extends RuntimeException {
    public AppNotFoundException(String message) {
        super(message);
    }
}
