package com.example.pmcore.dto.data.purchase;

import com.example.backend.model.data.finances.PurchaseType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
@Builder
@AllArgsConstructor
public class PurchaseHistoryDto {
    private String appName;

    @Enumerated(EnumType.STRING)
    private PurchaseType purchaseType;

    private LocalDate purchaseDate;
    private double purchasePrice;
    private String cardNumber;
}
