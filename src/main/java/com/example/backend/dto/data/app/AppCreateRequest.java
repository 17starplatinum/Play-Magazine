package com.example.backend.dto.data.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double price;

    @NotNull
    private AppType type;

    private Double subscriptionPrice;
    private Integer subscriptionDays;

    @Valid
    private AppRequirementsDto requirements;

    public enum AppType {
        FREE, PAID, SUBSCRIPTION
    }
}