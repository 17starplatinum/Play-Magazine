package com.example.backend.services.data;

import com.example.backend.dto.data.app.*;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.mappers.AppMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.app.AppFile;
import com.example.backend.model.data.app.AppRequirements;
import com.example.backend.model.data.app.AppVersion;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.repositories.auth.UserRepository;
import com.example.backend.repositories.data.app.AppFileRepository;
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.app.AppRequirementsRepository;
import com.example.backend.repositories.data.app.AppVersionRepository;
import com.example.backend.repositories.data.finances.PurchaseRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.FileUtils;
import com.example.backend.services.util.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;
    private final MinioService minioService;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final UserService userService;
    private final AppRequirementsRepository appRequirementsRepository;
    private final AppFileRepository appFileRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final AppMapper appMapper;
    private final AppVersionRepository appVersionRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;

    static final String APP_NOT_FOUND_MESSAGE = "Application not found!";
    public AppDownloadResponse prepareAppDownload(UUID appId) {
        App app = getAppById(appId);
        boolean updateAvailable = isNewerThan(app);
        return appMapper.mapToResponse(app, updateAvailable, getCurrentVersion(app));
    }

    public AppCompatibilityResponse checkCompatibility(UUID appId, String os) {
        App app = getAppById(appId);
        AppRequirements appRequirements = app.getAppRequirements();
        List<String> compatibilityIssues = new ArrayList<>();

        if (!appRequirements.getCompatibleOs().contains(os)) {
            compatibilityIssues.add(String.format(
                    "OS not supported by application: Target platform: %s, Device platform: %s",
                    appRequirements.getCompatibleOs(), os
            ));
        }

        return AppCompatibilityResponse.builder()
                .compatible(compatibilityIssues.isEmpty())
                .issues(compatibilityIssues)
                .build();
    }

    public List<AppDto> getAllAvailableApps(int limit) {
        List<AppDto> appsDto = new ArrayList<>();
        var apps = appRepository.findAppsLimit(limit);
        for (App app : apps) appsDto.add(appMapper.mapToDtoShort(app, reviewService::getAverageRating));
        return appsDto;
    }

    public AppInfoResponseDto getAppInfoById(UUID appId) {
        return appMapper.mapToDto(getAppById(appId), reviewService::getAverageRating);
    }

    public App getAppById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND_MESSAGE));
    }

    public String getAppNameById(UUID appId) {
        return getAppById(appId).getName();
    }

    public UUID createApp(AppCreateRequest appCreateRequest) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        MultipartFile file = appCreateRequest.getFile();
        User author = userService.getCurrentUser();

        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        AppFile appFile = appMapper.mapToAppFile(filePath, file);

        AppVersion version = new AppVersion("1.0", (appCreateRequest.getReleaseNotes() == null) ? "Initial release" : appCreateRequest.getReleaseNotes());

        AppRequirements requirements = AppRequirements.builder()
                .minRamMb(appCreateRequest.getMinRamMb())
                .minStorageMb(appCreateRequest.getMinStorageMb())
                .compatibleOs(appCreateRequest.getCompatibleOs())
                .build();

        appFile = appFileRepository.save(appFile);

        App app = appMapper.mapToModel(appCreateRequest, appFile, requirements, author, version);

        app = appRepository.save(app);
        version.setApp(app);
        appVersionRepository.save(version);
        appRequirementsRepository.save(requirements);
        transactionManager.commit(transaction);
        return app.getId();
    }

    public void bumpApp(UUID appId, AppUpdateDto appUpdateDto) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        var file = appUpdateDto.getFile();
        App app = getAppById(appId);
        User user = userService.getCurrentUser();
        AppFile appFile = app.getAppFile();
        AppVersion appVersion;
        if (!app.getAuthor().equals(user)) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("Suddenly, you aren`t the owner of this application");
        }

        try {
            minioService.deleteFile(appFile.getFilePath());
            String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());
            byte[] fileContent = file.getBytes();

            if (appUpdateDto.getReleaseNotes() == null || appUpdateDto.getReleaseNotes().isBlank()) {
                appVersion = new AppVersion(appUpdateDto.getNewVersion());
            } else {
                appVersion = new AppVersion(appUpdateDto.getNewVersion(), appUpdateDto.getReleaseNotes());
            }
            appVersion.setApp(app);
            app.getAppVersions().add(appVersion);
            appFile.setFileHash(filePath);
            appFile.setFileSize(file.getSize());
            appFile.setFileHash(FileUtils.calculateFileHash(fileContent));
            appFile.setLastUpdated(LocalDateTime.now());

            appFileRepository.save(appFile);
            appRepository.save(app);
            transactionManager.commit(transaction);
        } catch (IOException e) {
            transactionManager.rollback(transaction);
            throw new AppUpdateException("Cannot read application file", e);
        } catch (NoSuchAlgorithmException e) {
            transactionManager.rollback(transaction);
            throw new IllegalStateException("Cannot generate hash of this application", e);
        }
    }


    public boolean isNewerThan(App app) {
        return !Objects.equals(app.getLatestVersion().getVersion(), getCurrentVersion(app));
    }

    public String getCurrentVersion(App app) {
        Purchase purchase = purchaseService.getLastUserPurchaseByApp(app);
        return app.getAppVersions().size() == 1 ?
                app.getLatestVersion().getVersion() : purchase.getDownloadedVersion();
    }

    public byte[] downloadAppFile(UUID appId, UUID cardId, boolean forceUpdate) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        App app = getAppById(appId);
        User currentUser = userService.getCurrentUser();
        Purchase purchase = purchaseService.processPurchase(appId, cardId);
        if (app.getUsersWhoDownloaded().contains(purchase.getUser()) &&
                !forceUpdate && !isNewerThan(purchase.getApp())) {
            transactionManager.rollback(transaction);
            throw new AppUpToDateException("This is actual release");
        }
        AppFile appFile = app.getAppFile();
        if (app.getUsersWhoDownloaded().contains(purchase.getUser())) {
            minioService.deleteFile(appFile.getFilePath());
            appFile.setLastUpdated(LocalDateTime.now());
        }
        byte[] fileContent;
        try {
            fileContent = minioService.downloadFile(appFile.getFilePath());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new AppDownloadException("Failed to download the application", e);
        }

        userRepository.addAppToUser(currentUser.getId(), appId);

        purchase.setUser(currentUser);
        purchaseRepository.save(purchase);
        transactionManager.commit(transaction);
        return fileContent;
    }

    public void deleteApp(UUID appId) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userService.getCurrentUser();
        App app = getAppById(appId);
        if (!app.getAuthor().getEmail().equals(user.getUsername())) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("Suddenly, you aren`t the owner of this application");
        }
        app.getUsersWhoDownloaded().forEach(u -> u.removeApp(app));
        user.getDownloadedApps().forEach(a -> a.removeUser(user));
        appRepository.delete(app);
        userService.save(user);
        minioService.deleteFile(app.getAppFile().getFilePath());
        transactionManager.commit(transaction);
    }
}
