package com.example.pmweb.dto.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseMessage {
    private String operationId;
    private UUID appId;
    private UUID cardId;
}