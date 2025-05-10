package com.example.backend.exceptions.notfound;

public class UserBudgetNotFoundException extends RuntimeException{
    public UserBudgetNotFoundException(String message){
        super(message);
    }
}
