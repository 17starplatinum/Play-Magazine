package com.example.backend.dto.data.budget;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class BudgetDto {
    @DecimalMin("0.00")
    private float spendingLimit;
}
