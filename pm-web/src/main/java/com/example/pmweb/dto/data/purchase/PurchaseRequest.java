package com.example.pmweb.dto.data.purchase;

import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Getter
public class PurchaseRequest {

    private UUID appID;

    private UUID cardId;

    private UUID subscriptionId;
}
