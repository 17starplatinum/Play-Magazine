package com.example.backend.dto.data;

import jakarta.validation.constraints.*;
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
    @Min(value = 0L, message = "Цена должна быть неотрицательным")
    private Float price;

    private String description;
}
