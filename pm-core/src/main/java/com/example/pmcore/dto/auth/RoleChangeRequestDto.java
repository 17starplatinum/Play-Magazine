package com.example.pmcore.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RoleChangeRequestDto implements Serializable {
    @NotNull
    private String email;

    @NotNull
    private String role;

    private String requestStatus;

    @Size(max = 500)
    @JsonIgnore
    private String reason;
}
