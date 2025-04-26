package com.example.backend.mappers;

import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.model.auth.UserBudget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetStatusDto mapToDto(UserBudget budget, double remainder) {
        return BudgetStatusDto.builder()
                .budget(budget.getSpendingLimit())
                .spending(budget.getCurrentSpending())
                .remaining(remainder)
                .build();
    }

    public BudgetStatusDto mapToNewDto() {
        return BudgetStatusDto.builder()
                .budget(0D)
                .spending(0D)
                .remaining(0D)
                .build();
    }
}
