package com.example.backend.exceptions.paymentrequired;

public class AppNotPurchasedException extends RuntimeException {
    public AppNotPurchasedException(String message, Throwable cause) {
        super(message, cause);
    }
}
