package com.example.pmcore.exceptions.conflict;

public class SubscriptionAlreadyPurchasedException extends RuntimeException {
    public SubscriptionAlreadyPurchasedException(String message) {
        super(message);
    }
}
