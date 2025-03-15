package com.example.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class UserInfoResponse {
    private UUID id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthday;
}
