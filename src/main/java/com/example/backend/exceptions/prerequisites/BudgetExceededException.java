package com.example.backend.exceptions.prerequisites;

public class BudgetExceededException extends RuntimeException {
    public BudgetExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
