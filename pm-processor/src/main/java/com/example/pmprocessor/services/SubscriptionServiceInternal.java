package com.example.pmprocessor.services;

import java.util.UUID;

public interface SubscriptionServiceInternal {
    void createSubscription(UUID subscriptionId, UUID userId);
    void cancelSubscription(UUID subscriptionId, UUID userId);
    void renewSubscription(UUID subscriptionId, UUID userId);
}
