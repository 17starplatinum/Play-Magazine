package com.example.pmcore.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CodeVerificationRequest {
    @Size(min = 5, max = 32, message = "Адрес электронной почты должен содержать от 5 до 32 символов")
    @NotBlank(message = "Адрес электронной почты не может быть пустыми")
    @Email(message = "Email адрес должен быть в формате user@example.com")
    private String email;
    private String code;
    private String verificationCodeId;

    public UUID getVerificationCodeId() {
        try {
            return UUID.fromString(verificationCodeId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid verification code id!");
        }
    }
}
