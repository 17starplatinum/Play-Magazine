package com.example.pmcore.dto.data.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCreationDto {
    @NotNull
    @NotBlank
    private String name;

    @NotNull
    private UUID appId;

    private Double subscriptionPrice;

    private Integer subscriptionDays;

    private Boolean autoRenewal;
}
