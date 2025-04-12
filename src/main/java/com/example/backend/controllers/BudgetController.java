package com.example.backend.controllers;

import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.services.data.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetStatusDto> getBudgetStatus() {
        return ResponseEntity.ok(budgetService.getBudgetStatus());
    }

    @PostMapping("/limit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> setMonthlyLimit(@RequestParam double limit) {
        budgetService.setMonthlyLimit(limit);
        return ResponseEntity.ok(String.format("Месячный бюджет (%f) установлен.", limit));
    }
}
