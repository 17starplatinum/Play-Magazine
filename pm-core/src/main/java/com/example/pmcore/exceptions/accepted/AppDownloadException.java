package com.example.pmcore.exceptions.accepted;

public class AppDownloadException extends RuntimeException {
    public AppDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
