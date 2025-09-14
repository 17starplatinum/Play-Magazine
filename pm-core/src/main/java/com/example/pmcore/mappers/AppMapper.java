package com.example.pmcore.mappers;

import com.example.backend.dto.data.app.AppCreateRequest;
import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.dto.data.app.AppDto;
import com.example.backend.dto.data.app.AppInfoResponseDto;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.app.App;
import com.example.backend.model.data.app.AppFile;
import com.example.backend.model.data.app.AppRequirements;
import com.example.backend.model.data.app.AppVersion;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
public class AppMapper {

    public <T> AppInfoResponseDto mapToDto(App app, User author, Function<UUID, T> getAverageRating) {
        return AppInfoResponseDto.builder()
                .name(app.getName())
                .description(app.getDescription())
                .price(app.getPrice())
                .averageRating((Double) getAverageRating.apply(app.getId()))
                .releaseDate(app.getReleaseDate())
                .latestVersion(app.getLatestVersion().getVersion())
                .releaseNotes(app.getLatestVersion().getReleaseNotes())
                .ownerEmail(author.getEmail())
                .build();
    }

    public <T> AppDto mapToDtoShort(App app, Function<UUID, T> getAverageRating) {
        return AppDto.builder()
                .name(app.getName())
                .price(app.getPrice())
                .averageRating((Double) getAverageRating.apply(app.getId()))
                .build();
    }

    public AppDownloadResponse mapToResponse(App app, boolean updateAvailable, String currentVersion) {
        return AppDownloadResponse.builder()
                .appId(app.getId())
                .name(app.getName())
                .currentVersion(currentVersion)
                .availableVersion(app.getLatestVersion().getVersion())
                .updateAvailable(updateAvailable)
                .fileSize(app.getAppFile().getFileSize())
                .fileHash(app.getAppFile().getFileHash())
                .build();
    }

    public App mapToModel(AppCreateRequest appCreateRequest, AppFile appFile, AppRequirements appRequirements, User author, AppVersion version) {
        return App.builder()
                .name(appCreateRequest.getName())
                .description(appCreateRequest.getDescription())
                .price(appCreateRequest.getPrice())
                .releaseDate(LocalDate.now())
                .authorId(author.getId())
                .appFile(appFile)
                .appRequirements(appRequirements)
                .appVersions(new ArrayList<>(List.of(version)))
                .build();
    }

    public AppFile mapToAppFile(String filePath, MultipartFile file) {
        return AppFile.builder()
                .filePath(filePath)
                .fileSize(file.getSize())
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
