package com.example.backend.dto.data.purchase;

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
    private LocalDate purchaseDate;
    private double purchasePrice;
    private String cardNumber;
}
