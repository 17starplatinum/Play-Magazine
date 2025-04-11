package com.example.backend.exceptions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String message;
    private long timestamp;
}
