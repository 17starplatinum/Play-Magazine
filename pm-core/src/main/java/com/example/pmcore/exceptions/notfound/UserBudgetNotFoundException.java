package com.example.pmcore.exceptions.notfound;

public class UserBudgetNotFoundException extends RuntimeException{
    public UserBudgetNotFoundException(String message){
        super(message);
    }
}
