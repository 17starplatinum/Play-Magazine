package com.example.pmcore.exceptions.accepted;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
