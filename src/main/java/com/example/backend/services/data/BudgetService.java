package com.example.backend.services.data;

import com.example.backend.dto.data.budget.BudgetDto;
import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.exceptions.prerequisites.BudgetExceededException;
import com.example.backend.repositories.PurchaseRepository;
import com.example.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public void setMonthlyLimit(BudgetDto budgetDto, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        user.setSpendingLimit(budgetDto.getSpendingLimit());
        resetSpendingIfNeeded(user);
        userRepository.save(user);
    }

    public BudgetStatusDto getBudgetStatus(UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        resetSpendingIfNeeded(user);
        return BudgetStatusDto.builder()
                .budget(user.getSpendingLimit())
                .spending(user.getCurrentSpending())
                .remaining(calculateRemaining(user))
                .build();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void resetAllSpending() {
        userRepository.findAll().forEach(user -> {
            user.setCurrentSpending(0F);
            user.setLastLimitReset(LocalDate.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordSpending(User user, float limit) {
        resetSpendingIfNeeded(user);

        if (isOverBudget(user, limit)) {
            throw new BudgetExceededException(
                    String.format("Месячный бюджет превышен. Бюджет: %.2f, потрачено: %.2f",
                            user.getSpendingLimit(),
                            user.getCurrentSpending()
                    ), new RuntimeException());
        }

        user.setCurrentSpending(user.getCurrentSpending() + limit);
        userRepository.save(user);
    }

    private void resetSpendingIfNeeded(User user) {
        if (user.getLastLimitReset().getMonth() != LocalDate.now().getMonth() ||
                user.getLastLimitReset().getYear() != LocalDate.now().getYear()) {
            user.setCurrentSpending(0F);
            user.setLastLimitReset(LocalDate.now());
        }

    }

    private boolean isOverBudget(User user, float amount) {
        if (user.getSpendingLimit() == null) {
            return false;
        }
        float projected = user.getCurrentSpending() + amount;
        return projected > user.getSpendingLimit();
    }

    private Float calculateRemaining(User user) {
        if (user.getSpendingLimit() == null) {
            return null;
        }
        return user.getSpendingLimit() - user.getCurrentSpending();
    }
}
