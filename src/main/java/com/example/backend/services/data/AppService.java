package com.example.backend.services.data;

import com.example.backend.dto.data.app.AppCreateRequest;
import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.dto.data.app.AppInfoResponseDto;
import com.example.backend.dto.data.app.AppUpdateDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.exceptions.prerequisites.InvalidApplicationConfigException;
import com.example.backend.mappers.AppMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.app.AppFile;
import com.example.backend.model.data.app.AppRequirements;
import com.example.backend.model.data.app.AppVersion;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.repositories.data.ReviewRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.backend.dto.data.app.AppCreateRequest.AppType.SUBSCRIPTION;

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
    private final ReviewService reviewService;
    private final AppMapper appMapper;
    private final AppVersionRepository appVersionRepository;

    public AppDownloadResponse prepareAppDownload(UUID appId) {
        App app = getAppById(appId);
        boolean updateAvailable = app.isNewerThan(app.getPreviousVersion().getApp());
        return appMapper.mapToResponse(app, updateAvailable);
    }

    public AppCompatibilityResponse checkCompatibility(UUID appId, String os) {
        App app = getAppById(appId);
        AppRequirements appRequirements = appRequirementsRepository.findAppRequirementsByApp(app);
        List<String> compatibilityIssues = new ArrayList<>();

        //SystemInfo si = new SystemInfo();

        // Тут проверка os сервера а не клиента, это не имеет смысла
        // Клиент - Android, Приложение для Android, Сервер - Windows => скачать не смогу так как Windows != Android
        // Соответственно все что ниже тоже
        // OperatingSystem os = si.getOperatingSystem();
        /*GlobalMemory memory = si.getHardware().getMemory();
        List<HWDiskStore> diskStores = si.getHardware().getDiskStores();

        if (appRequirements.getMinRamMb() > memory.getTotal()) {
            compatibilityIssues.add(String.format(
                    "RAM shortage: %dMB required, %dMB available.",
                    appRequirements.getMinRamMb(), memory.getTotal()
            ));
        }

        boolean enoughDiskSpace = false;
        for (HWDiskStore diskStore : diskStores) {
            if (appRequirements.getMinStorageMb() < diskStore.getSize()) {
                enoughDiskSpace = true;
                break;
            }
        }

        if (!enoughDiskSpace) {
            for (HWDiskStore diskStore : diskStores) {
                compatibilityIssues.add(String.format(
                        "Disk space shortage: %dMB required, %dMB available.",
                        appRequirements.getMinStorageMb(), diskStore.getSize()
                ));
            }
        }*/

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

    public List<AppInfoResponseDto> getAllAvailableApps(int limit) {
        List<AppInfoResponseDto> appsDto = new ArrayList<>();
        var apps = appRepository.findAppsLimit(limit);
        for (App app : apps) appsDto.add(appMapper.mapToDto(app, reviewService::getAverageRating));
        return appsDto;
    }

    public AppInfoResponseDto getAppInfoById(UUID appId) {
        return appMapper.mapToDto(getAppById(appId), reviewService::getAverageRating);
    }

    public App getAppById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Application not found!"));
    }

    public App getAppByName(String name) {
        return appRepository.findByName(name);
    }

    @Transactional
    public UUID createApp(AppCreateRequest appCreateRequest) {
        MultipartFile file = appCreateRequest.getFile();
        User author = userService.getCurrentUser();

        if (appCreateRequest.getType() == SUBSCRIPTION &&
                (appCreateRequest.getSubscriptionPrice() == null || appCreateRequest.getSubscriptionPrice() <= 0)) {
            throw new InvalidApplicationConfigException("Subscription price must be positive!");
        }

        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        AppFile appFile = appMapper.mapToAppFile(filePath, file);

        AppVersion version = new AppVersion("1.0", "Initial release");
        appFile = appFileRepository.save(appFile);

        App app = appMapper.mapToModel(appCreateRequest, appFile, author, version);

        app = appRepository.save(app);
        version.setApp(app);
        appVersionRepository.save(version);

        AppRequirements requirements = AppRequirements.builder()
                .minRamMb(appCreateRequest.getMinRamMb())
                .minStorageMb(appCreateRequest.getMinStorageMb())
                .compatibleOs(appCreateRequest.getCompatibleOs())
                .build();

        requirements.setApp(app);
        appRequirementsRepository.save(requirements);

        return app.getId();
    }

    @Transactional
    public void bumpApp(UUID appId, AppUpdateDto appUpdateDto) {
        var file = appUpdateDto.getFile();
        App app = getAppById(appId);
        User user = userService.getCurrentUser();
        AppFile appFile = app.getAppFile();
        AppVersion appVersion;
        if (!app.getAuthor().equals(user)) {
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

            app.getAppVersions().add(appVersion);
            appFile.setFileHash(filePath);
            appFile.setFileSize(file.getSize());
            appFile.setFileHash(FileUtils.calculateFileHash(fileContent));
            appFile.setLastUpdated(LocalDateTime.now());

            appFileRepository.save(appFile);
            appRepository.save(app);
        } catch (IOException e) {
            throw new AppUpdateException("Cannot read application file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot generate hash of this application", e);
        }
    }

    @Transactional
    public byte[] downloadAppFile(UUID appId, boolean forceUpdate) {
        App app = getAppById(appId);
        User user = userService.getCurrentUser();
        AppFile appFile = app.getAppFile();
        Purchase purchase = purchaseService.getPurchaseByUserAndApp(user, app);

        if (!forceUpdate && !app.isNewerThan(purchase.getApp())) {
            throw new AppUpToDateException("This is actual release");
        }
        try {
            minioService.deleteFile(appFile.getFilePath());
            byte[] fileContent = minioService.downloadFile(appFile.getFilePath());
            appFile.setLastUpdated(LocalDateTime.now());
            purchaseRepository.save(purchase);
            return fileContent;
        } catch (Exception e) {
            throw new AppDownloadException("Something went wrong while downloading the application", e);
        }
    }


    // TODO: допиши удаление зависимых от app сущностей
    public void deleteApp(UUID appId) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(appId);
        if (!app.getAuthor().getEmail().equals(currentUser.getUsername())) {
            // && !(currentUser.getRole().equals(Role.MODERATOR) || currentUser.getRole().equals(Role.ADMIN))) {
            throw new AccessDeniedException("Suddenly, you aren`t the owner of this application");
        }

        minioService.deleteFile(app.getAppFile().getFilePath());
        reviewService.deleteReviews(reviewService.getAppReviews(app));
        appRepository.delete(app);
    }
}
