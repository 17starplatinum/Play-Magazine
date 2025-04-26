package com.example.backend.exceptions.conflict;

public class AlreadyInRoleException extends RuntimeException {
    public AlreadyInRoleException(String message) {
        super(message);
    }
}
