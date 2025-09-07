package com.example.pmweb.dto.data.app;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDownloadResponse {
    @NotNull
    private UUID appId;
    @NotNull
    private String name;
    @NotNull
    private String currentVersion;
    @NotNull
    private String availableVersion;

    private boolean updateAvailable;

    private Long fileSize;

    private String fileHash;
}
