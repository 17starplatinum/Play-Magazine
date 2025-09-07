package com.example.pmweb.dto.data.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponseDto {
    private UUID id;
    private String name;
    private String appName;
    private Double fee;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private Integer daysRemaining;
    private boolean autoRenewal;
}
