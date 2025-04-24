package com.example.backend.dto.data.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    @NotNull
    @Size(min = 16, max = 19)
    @Pattern(regexp = "\\d+")
    private String number;

    @NotNull
    @Pattern(regexp = "^\\d{3}$")
    private String cvv;

    @NotNull
    @Future
    private LocalDate expiryDate;
}
