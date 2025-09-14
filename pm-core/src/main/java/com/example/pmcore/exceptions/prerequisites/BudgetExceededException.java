package com.example.pmcore.exceptions.prerequisites;

public class BudgetExceededException extends RuntimeException {
    public BudgetExceededException(String message) {
        super(message);
    }
}
