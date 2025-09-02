package com.example.backend.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusResponse {
    private String requestStatus;
    private String role;
}
