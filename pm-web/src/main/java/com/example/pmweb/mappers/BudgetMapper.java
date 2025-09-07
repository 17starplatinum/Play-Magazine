package com.example.pmweb.mappers;

import com.example.pmweb.dto.data.budget.BudgetStatusDto;
import com.example.pmweb.model.auth.UserBudget;
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
