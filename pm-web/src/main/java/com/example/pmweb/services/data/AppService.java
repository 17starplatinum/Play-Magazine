package com.example.pmweb.services.data;

import com.example.pmweb.dto.data.app.*;
import com.example.pmweb.dto.util.AppCompatibilityResponse;
import com.example.pmweb.exceptions.accepted.AppDownloadException;
import com.example.pmweb.exceptions.accepted.AppUpdateException;
import com.example.pmweb.exceptions.notfound.AppNotFoundException;
import com.example.pmweb.exceptions.prerequisites.AppUpToDateException;
import com.example.pmweb.mappers.AppMapper;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.app.App;
import com.example.pmweb.model.data.app.AppFile;
import com.example.pmweb.model.data.app.AppRequirements;
import com.example.pmweb.model.data.app.AppVersion;
import com.example.pmweb.model.data.finances.Purchase;
import com.example.pmweb.repositories.auth.UserRepository;
import com.example.pmweb.repositories.data.app.AppFileRepository;
import com.example.pmweb.repositories.data.app.AppRepository;
import com.example.pmweb.repositories.data.app.AppRequirementsRepository;
import com.example.pmweb.repositories.data.app.AppVersionRepository;
import com.example.pmweb.repositories.data.finances.PurchaseRepository;
import com.example.pmweb.services.util.FileUtils;
import com.example.pmweb.services.util.MinioService;
import com.example.pmweb.services.auth.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

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
    @PersistenceContext
    private final EntityManager em;
    static final String APP_NOT_FOUND_MESSAGE = "Application not found!";
    private final Logger logger = LogManager.getLogger();
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
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setReadOnly(false);

        TransactionStatus tx = transactionManager.getTransaction(def);
        String filePath = null;
        try {
            User current = userService.getCurrentUser();

            if (current == null) {
                throw new AccessDeniedException("No current user");
            }
            User managedCurrent = em.find(User.class, current.getId());
            if (managedCurrent == null) {
                throw new EntityNotFoundException("Current user not found in DB");
            }

            App app = em.createQuery(
                                "SELECT a FROM App a LEFT JOIN FETCH a.usersWhoDownloaded WHERE a.id = :id", App.class)
                        .setParameter("id", appId)
                        .getResultStream().findFirst().orElseThrow(() -> new EntityNotFoundException("App not found: " + appId));

            if (!app.getAuthor().getEmail().equals(managedCurrent.getUsername())) {
                throw new AccessDeniedException("Not owner");
            }

            if (app.getAppFile() != null) {
                filePath = app.getAppFile().getFilePath();
            }

            Iterator<User> it = app.getUsersWhoDownloaded().iterator();

            while (it.hasNext()) {
                User u = it.next();
                it.remove();
                User mu = em.contains(u) ? u : em.find(User.class, u.getId());
                if (mu == null) {
                    logger.warn("User not found for id={}", u == null ? "null" : u.getId());
                } else {
                    boolean removed = mu.getDownloadedApps().removeIf(a -> a.getId().equals(appId));
                    logger.debug("Removed from owning side for user {} ? {}", mu.getId(), removed);
                }
            }
            em.flush();

            if (!em.contains(app)) {
                app = em.merge(app);
            }
            em.remove(app);
            em.flush();
            transactionManager.commit(tx);

            if (filePath != null) {
                try {
                    minioService.deleteFile(filePath);
                    logger.info("Minio file deleted {}", filePath);
                } catch (Exception e) {
                    logger.error("Failed to delete file in MinIO (post-commit) {}", filePath, e);
                }
            }
        } catch (Exception ex) {
            logger.error("deleteApp FAILED for appId={}. About to rollback. Exception: ", appId, ex);
            try {
                if (!tx.isCompleted()) {
                    transactionManager.rollback(tx);
                    logger.info("Transaction rolled back");
                } else {
                    logger.warn("Transaction already completed when catch encountered");
                }
            } catch (Exception rbEx) {
                logger.error("Rollback failed", rbEx);
            }
        }
    }
}
