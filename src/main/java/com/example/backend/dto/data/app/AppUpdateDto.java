package com.example.backend.dto.data.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AppUpdateDto {
    @NotNull
    @NotBlank
    private String newVersion;
    private String releaseNotes;
}
