package com.example.pmcore.exceptions.conflict;

public class AlreadyInRoleException extends RuntimeException {
    public AlreadyInRoleException(String message) {
        super(message);
    }
}
