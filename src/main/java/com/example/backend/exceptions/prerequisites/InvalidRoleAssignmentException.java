package com.example.backend.exceptions.prerequisites;

public class InvalidRoleAssignmentException extends RuntimeException {
    public InvalidRoleAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
