package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
public class RoleChangeRequestDto {
    @NotNull
    private UUID userId;

    @NotNull
    private String email;

    @NotNull
    private String role;

    @NotNull
    private String requestStatus;

    @Size(max = 500)
    private String reason;
}
