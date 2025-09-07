package com.example.pmweb.exceptions.notfound;

public class UserBudgetNotFoundException extends RuntimeException{
    public UserBudgetNotFoundException(String message){
        super(message);
    }
}
