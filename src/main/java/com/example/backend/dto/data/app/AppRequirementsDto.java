package com.example.backend.dto.data.app;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class AppRequirementsDto {
    @Positive
    private Integer minRamMb;

    @Positive
    private Integer minStorageMb;

    @NotEmpty
    private List<String> compatibleOs;
}
