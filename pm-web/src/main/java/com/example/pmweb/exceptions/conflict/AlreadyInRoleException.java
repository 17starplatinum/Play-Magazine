package com.example.pmweb.exceptions.conflict;

public class AlreadyInRoleException extends RuntimeException {
    public AlreadyInRoleException(String message) {
        super(message);
    }
}
