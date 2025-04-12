package com.example.backend.exceptions.prerequisites;

public class AlreadyInRoleException extends RuntimeException {
    public AlreadyInRoleException(String message) {
        super(message);
    }
}
