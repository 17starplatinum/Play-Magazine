package com.example.backend.exceptions.accepted;

public class AppDownloadException extends RuntimeException {
    public AppDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
