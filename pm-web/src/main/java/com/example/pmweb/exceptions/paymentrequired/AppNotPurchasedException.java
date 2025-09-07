package com.example.pmweb.exceptions.paymentrequired;

public class AppNotPurchasedException extends RuntimeException {
    public AppNotPurchasedException(String message) {
        super(message);
    }
}
