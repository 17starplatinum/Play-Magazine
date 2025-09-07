package com.example.pmweb.exceptions.conflict;

public class SubscriptionAlreadyPurchasedException extends RuntimeException {
    public SubscriptionAlreadyPurchasedException(String message) {
        super(message);
    }
}
