package com.example.backend.controllers;

import com.example.backend.dto.data.ResponseDto;
import com.example.backend.dto.data.budget.BudgetStatusDto;
import com.example.backend.dto.data.budget.MonthlyLimitDto;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.data.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<BudgetStatusDto> getBudgetStatus() {
        return ResponseEntity.ok(budgetService.getBudgetStatus());
    }

    @PostMapping("/limit")
    public ResponseEntity<?> setMonthlyLimit(@RequestBody MonthlyLimitDto monthlyLimitDto) {
        double limit = monthlyLimitDto.getLimit();
        budgetService.setMonthlyLimit(limit);

        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);

        String formattedLimit = df.format(limit);

        return ResponseEntity.ok().body(new ResponseDto(String.format("Месячный бюджет %s установлен.", formattedLimit)));
    }
}
