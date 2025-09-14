package com.example.pmcore.services.data;

import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.exceptions.prerequisites.BudgetExceededException;
import com.example.backend.mappers.BudgetMapper;
import com.example.backend.model.auth.UserBudget;
import com.example.backend.repositories.auth.file.FileBasedUserBudgetRepository;
import com.example.pmcore.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final FileBasedUserBudgetRepository userBudgetRepository;
    private final UserService userService;
    private final BudgetMapper budgetMapper;

    public UserBudget getUserBudget() {
        return userBudgetRepository.findById(userService.getCurrentUser().getUserBudgetId()).orElse(null);
    }

    public void setMonthlyLimit(Double limit) {
        UserBudget userBudget = getUserBudget();
        userBudget.setSpendingLimit(limit);
        resetSpendingIfNeeded(userBudget);
        userBudgetRepository.save(userBudget);
    }

    public BudgetStatusDto getBudgetStatus() {
        UserBudget userBudget = getUserBudget();
        resetSpendingIfNeeded(userBudget);
        double remainder = calculateRemaining(userBudget);
        return budgetMapper.mapToDto(userBudget, remainder);
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void resetAllSpending() {
        userBudgetRepository.findAll().forEach(user -> {
            user.setCurrentSpending(0D);
            user.setLastLimitReset(LocalDate.now());
            userBudgetRepository.save(user);
        });
    }

    public void recordSpending(UserBudget userBudget, double limit) {
        resetSpendingIfNeeded(userBudget);
        if (userBudget.getSpendingLimit() != null && isOverBudget(userBudget, limit)) {
            throw new BudgetExceededException(
                    String.format("Месячный бюджет превышен. Бюджет: %.2f, потрачено: %.2f",
                            userBudget.getSpendingLimit(),
                            userBudget.getCurrentSpending()
                    ));
        }

        userBudget.setCurrentSpending(userBudget.getCurrentSpending() + limit);
        userBudgetRepository.save(userBudget);
    }

    private void resetSpendingIfNeeded(UserBudget userBudget) {
        if (userBudget.getLastLimitReset().getMonth() != LocalDate.now().getMonth() ||
                userBudget.getLastLimitReset().getYear() != LocalDate.now().getYear()) {
            userBudget.setCurrentSpending(0D);
            userBudget.setLastLimitReset(LocalDate.now());
        }
    }

    private boolean isOverBudget(UserBudget userBudget, double amount) {
        if (userBudget.getSpendingLimit() == null) {
            return false;
        }
        double projected = userBudget.getCurrentSpending() + amount;
        return projected > userBudget.getSpendingLimit();
    }

    public Double calculateRemaining(UserBudget userBudget) {
        if (userBudget.getSpendingLimit() == null) {
            return 0.0;
        }
        return userBudget.getSpendingLimit() - userBudget.getCurrentSpending();
    }
}
