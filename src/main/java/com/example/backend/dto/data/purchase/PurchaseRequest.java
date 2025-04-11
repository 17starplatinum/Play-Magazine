package com.example.backend.dto.data.purchase;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Getter
public class PurchaseRequest {
    @NotNull
    private UUID appId;

    @NotNull
    private UUID cardId;
}
