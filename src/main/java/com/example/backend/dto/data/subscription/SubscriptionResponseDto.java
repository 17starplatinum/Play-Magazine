package com.example.backend.dto.data.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Setter
@Builder
@AllArgsConstructor
public class SubscriptionResponseDto {
    private UUID id;
    private String name;
    private String appName;
    private Double fee;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer daysRemaining;
    private boolean autoRenewal;
    private boolean active;
}
