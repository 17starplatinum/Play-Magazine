package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequestDto {
    @NotNull
    private UUID userId;

    @NotNull
    private String newRole;

    @Size(max = 500)
    private String reason;
}
