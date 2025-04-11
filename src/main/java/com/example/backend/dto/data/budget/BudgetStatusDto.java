package com.example.backend.dto.data.budget;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetStatusDto {
    private Float budget;
    private Float spending;
    private Float remaining;
}
