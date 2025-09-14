package com.example.pmcore.mappers;

import com.example.pmcore.dto.data.budget.BudgetStatusDto;
import com.example.pmcore.model.auth.UserBudget;
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
