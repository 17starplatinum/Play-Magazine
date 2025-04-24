package com.example.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeVerificationResponse {
    private String email;
    private String verificationCodeId;
}
