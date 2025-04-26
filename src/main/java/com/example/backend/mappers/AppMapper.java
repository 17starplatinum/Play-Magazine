package com.example.backend.mappers;

import com.example.backend.dto.data.app.AppCreateRequest;
import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.app.AppFile;
import com.example.backend.model.data.app.AppVersion;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppMapper {
    public AppDownloadResponse mapToResponse(App app, boolean updateAvailable) {
        return AppDownloadResponse.builder()
                .appId(app.getId())
                .name(app.getName())
                .currentVersion(app.getPreviousVersion().getVersion())
                .availableVersion(app.getLatestVersion().getVersion())
                .updateAvailable(updateAvailable)
                .fileSize(app.getAppFile().getFileSize())
                .fileHash(app.getAppFile().getFileHash())
                .build();
    }

    public App mapToModel(AppCreateRequest appCreateRequest, AppFile appFile, User author, AppVersion version) {
        return App.builder()
                .name(appCreateRequest.getName())
                .description(appCreateRequest.getDescription())
                .price(appCreateRequest.getPrice())
                .releaseDate(LocalDate.now())
                .author(author)
                .appFile(appFile)
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
