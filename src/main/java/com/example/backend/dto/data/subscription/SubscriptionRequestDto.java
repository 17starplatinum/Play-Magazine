package com.example.backend.dto.data.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequestDto {

    private UUID appId;

    @NotNull
    private UUID cardId;

    @NotNull
    private String name;

    @NotNull
    @NotBlank
    private String appName;

    @NotNull
    private Double fee;

    @NotNull
    private Integer days;

    private Boolean autoRenewal;
}
