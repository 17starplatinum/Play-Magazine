package com.example.pmprocessor.services;

import java.util.UUID;

public interface PurchaseServiceInternal {
    void processPurchase(UUID appId, UUID cardId);
}
