package com.example.backend.services.data;

import com.example.backend.dto.data.app.*;
import com.example.backend.dto.data.purchase.PurchaseRequest;
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
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import com.example.backend.repositories.data.app.AppFileRepository;
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.app.AppRequirementsRepository;
import com.example.backend.repositories.data.app.AppVersionRepository;
import com.example.backend.repositories.data.finances.PurchaseRepository;
import com.example.backend.repositories.data.subscription.UserSubscriptionRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.FileUtils;
import com.example.backend.services.util.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final UserSubscriptionRepository userSubscriptionRepository;

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
                .orElseThrow(() -> new AppNotFoundException("Application not found!"));
    }

    public App getAppByName(String name) {
        return appRepository.findByName(name);
    }

    public String getAppNameById(UUID appId) {
        return getAppById(appId).getName();
    }

    @Transactional
    public UUID createApp(AppCreateRequest appCreateRequest) {
        MultipartFile file = appCreateRequest.getFile();
        User author = userService.getCurrentUser();
        UserSubscription userSubscription = null;

        if (appCreateRequest.getType() == SUBSCRIPTION && appCreateRequest.getCreationDto() != null) {
            if (appCreateRequest.getCreationDto().getSubscriptionPrice() == null ||
                        appCreateRequest.getCreationDto().getSubscriptionPrice() <= 0) {
                throw new InvalidApplicationConfigException("Subscription price must be positive!");
            }
            userSubscription = UserSubscription.builder()
                    .invoice(
                            Invoice.builder()
                                    .amount(appCreateRequest.getCreationDto().getSubscriptionPrice()).build()
                    )
                    .days(appCreateRequest.getCreationDto().getSubscriptionDays())
                    .autoRenewal(appCreateRequest.getCreationDto().getAutoRenewal())
                    .build();
            userSubscriptionRepository.save(userSubscription);
        }


        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        AppFile appFile = appMapper.mapToAppFile(filePath, file);

        AppVersion version = new AppVersion("1.0", "Initial release");

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

        if(appCreateRequest.getCreationDto() != null && userSubscription != null) {
            Subscription subscription = Subscription.builder()
                    .name(appCreateRequest.getCreationDto().getName())
                    .app(app)
                    .build();
            userSubscription.setSubscription(subscription);
        }

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
            appVersion.setApp(app);
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


    public boolean isNewerThan(App app) {
        return !Objects.equals(app.getLatestVersion().getVersion(), getCurrentVersion(app));
    }

    public String getCurrentVersion(App app) {
        Purchase purchase = purchaseService.getLastUserPurchaseByApp(app);
        return app.getAppVersions().size() == 1 ?
                app.getLatestVersion().getVersion() : purchase.getDownloadedVersion();
    }

    @Transactional
    public byte[] downloadAppFile(UUID appId, PurchaseRequest purchaseRequest, boolean forceUpdate) {
        App app = getAppById(appId);
        AppFile appFile = app.getAppFile();
        Purchase purchase = purchaseService.processPurchase(appId, purchaseRequest);

        if (app.getUsersWhoDownloaded().contains(purchase.getUser()) &&
                !forceUpdate && !isNewerThan(purchase.getApp())) {
            throw new AppUpToDateException("This is actual release");
        }

        try {
            if(app.getUsersWhoDownloaded().contains(purchase.getUser())) {
                minioService.deleteFile(appFile.getFilePath());
                appFile.setLastUpdated(LocalDateTime.now());
            }
            byte[] fileContent = minioService.downloadFile(appFile.getFilePath());
            purchaseRepository.save(purchase);
            return fileContent;
        } catch (Exception e) {
            throw new AppDownloadException("Something went wrong while downloading the application", e);
        }
    }

    public void deleteApp(UUID appId) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(appId);
        if (!app.getAuthor().getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("Suddenly, you aren`t the owner of this application");
        }
        appRepository.delete(app);
        minioService.deleteFile(app.getAppFile().getFilePath());
    }
}
