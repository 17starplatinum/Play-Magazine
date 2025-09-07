package com.example.pmcommon.dto.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionMessage {
    private UUID operationId;
    private UUID subscriptionId;
    private UUID userId;
    private SubscriptionAction action;
}
