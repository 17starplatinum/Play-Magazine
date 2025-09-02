package com.example.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleChangeRequestDto {
    @NotNull
    private String email;

    @NotNull
    private String role;

    private String requestStatus;

    @Size(max = 500)
    @JsonIgnore
    private String reason;
}
