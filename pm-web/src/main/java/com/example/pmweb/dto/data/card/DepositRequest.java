package com.example.pmweb.dto.data.card;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Getter
public class DepositRequest {
    @NotNull
    private UUID cardId;

    @DecimalMin("0.01")
    private float amount;
}
