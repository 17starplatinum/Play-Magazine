package com.example.pmcore.dto.data.budget;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetStatusDto {
    private Double budget;
    private Double spending;
    private Double remaining;
}
