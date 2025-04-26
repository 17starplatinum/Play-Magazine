package com.example.backend.mappers;

import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.model.auth.UserBudget;
import com.example.backend.services.data.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {
    private BudgetService budgetService;

    @Autowired
    public void setBudgetService(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    public BudgetStatusDto mapToDto(UserBudget budget) {
        return BudgetStatusDto.builder()
                .budget(budget.getSpendingLimit())
                .spending(budget.getCurrentSpending())
                .remaining(budgetService.calculateRemaining(budget))
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
