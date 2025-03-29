package com.example.backend.dto.data.app;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDto {
    @NotBlank
    @Size(min = 2, max = 32)
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private Float price;

    @DecimalMin("0.00")
    private Float subscriptionPrice;

    private String description;

    private boolean isSubscription;
}
