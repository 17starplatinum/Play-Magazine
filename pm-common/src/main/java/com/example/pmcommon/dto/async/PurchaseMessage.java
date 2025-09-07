package com.example.pmcommon.dto.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseMessage {
    private UUID operationId;
    private UUID appId;
    private UUID cardId;
}