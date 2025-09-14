package com.example.pmcore.exceptions.paymentrequired;

public class AppNotPurchasedException extends RuntimeException {
    public AppNotPurchasedException(String message) {
        super(message);
    }
}
