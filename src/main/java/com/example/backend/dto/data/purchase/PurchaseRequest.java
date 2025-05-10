package com.example.backend.dto.data.purchase;

import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Getter
public class PurchaseRequest {

    private UUID cardId;

    private UUID subscriptionId;
}
