package com.example.pmweb.exceptions.conflict;

public class AppAlreadyPurchasedException extends RuntimeException {
    public AppAlreadyPurchasedException(String message) {
        super(message);
    }
}
