package com.example.backend.services.data;

import com.example.backend.dto.data.app.AppCreateRequest;
import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.dto.data.app.AppUpdateDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.exceptions.prerequisites.InvalidApplicationConfigException;
import com.example.backend.mappers.AppMapper;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.app.AppFile;
import com.example.backend.model.data.app.AppRequirements;
import com.example.backend.model.data.app.AppVersion;
import com.example.backend.model.data.finances.Purchase;
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
    private final AppMapper appMapper;
    private final AppVersionRepository appVersionRepository;

    public AppDownloadResponse prepareAppDownload(UUID appId) {
        App app = getAppById(appId);
        boolean updateAvailable = app.isNewerThan(app.getPreviousVersion().getApp());
        return appMapper.mapToResponse(app, updateAvailable);
    }

    public AppCompatibilityResponse checkCompatibility(UUID appId) {
        App app = getAppById(appId);
        AppRequirements appRequirements = appRequirementsRepository.findAppRequirementsByApp(app);
        List<String> compatibilityIssues = new ArrayList<>();

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        GlobalMemory memory = si.getHardware().getMemory();
        List<HWDiskStore> diskStores = si.getHardware().getDiskStores();

        if(appRequirements.getMinRamMb() > memory.getTotal()){
            compatibilityIssues.add(String.format(
                    "Не хватает памяти ОЗУ: требуется %dМб, а доступно только %dМб",
                    appRequirements.getMinRamMb(), memory.getTotal()
            ));
        }

        boolean enoughDiskSpace = false;
        for(HWDiskStore diskStore : diskStores) {
            if(appRequirements.getMinStorageMb() < diskStore.getSize()){
                enoughDiskSpace = true;
                break;
            }
        }

        if(!enoughDiskSpace){
            for(HWDiskStore diskStore : diskStores) {
                compatibilityIssues.add(String.format(
                        "Не хватает пространства на диске: требуется %dМб, а доступно только %dМб",
                        appRequirements.getMinStorageMb(), diskStore.getSize()
                ));
            }
        }

        if(!appRequirements.getCompatibleOs().contains(os.getFamily())) {
            compatibilityIssues.add(String.format(
                    "ОС не поддерживается приложением: целевая платформа: %s, но на устройстве %s",
                    appRequirements.getCompatibleOs(), os.getFamily()
            ));
        }

        return AppCompatibilityResponse.builder()
                .compatible(compatibilityIssues.isEmpty())
                .issues(compatibilityIssues)
                .build();
    }

    public List<App> getAllAvailableApps() {
        return appRepository.findAll();
    }

    public App getAppById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено"));
    }

    public App getAppByName(String name) {
        return appRepository.findByName(name);
    }

    @Transactional
    public App createApp(AppCreateRequest appCreateRequest, MultipartFile file) {
        User author = userService.getCurrentUser();

        if (appCreateRequest.getType() == SUBSCRIPTION &&
                (appCreateRequest.getSubscriptionPrice() == null || appCreateRequest.getSubscriptionPrice() <= 0)) {
            throw new InvalidApplicationConfigException("Цена подписки должна быть положительным числом");
        }

        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        AppRequirements requirements = AppRequirements.builder()
                .minRamMb(appCreateRequest.getRequirements().getMinRamMb())
                .minStorageMb(appCreateRequest.getRequirements().getMinStorageMb())
                .compatibleOs(appCreateRequest.getRequirements().getCompatibleOs())
                .build();

        AppFile appFile = appMapper.mapToAppFile(filePath, file);

        AppVersion version = new AppVersion("1.0", "Первый запуск.");

        App app = appMapper.mapToModel(appCreateRequest, appFile, author, version);

        version.setApp(app);
        requirements.setApp(app);
        appFile.setApp(app);

        return appRepository.save(app);
    }

    @Transactional
    public App bumpApp(UUID appId, AppUpdateDto appUpdateDto, MultipartFile file) {
        App app = getAppById(appId);
        User user = userService.getCurrentUser();
        AppFile appFile = app.getAppFile();
        AppVersion appVersion;
        if (!app.getAuthor().equals(user)) {
            throw new AccessDeniedException("Вы не являетесь создателем приложения");
        }

        try {
            minioService.deleteFile(appFile.getFilePath());
            String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());
            byte[] fileContent = file.getBytes();

            if(appUpdateDto.getReleaseNotes() == null || appUpdateDto.getReleaseNotes().isBlank()) {
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
            return appRepository.save(app);
        } catch (IOException e) {
            throw new AppUpdateException("Не удалось читать содержимое файла", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Не удалось вычислить хэш файла", e);
        }
    }

    @Transactional
    public byte[] downloadAppFile(UUID appId, boolean forceUpdate) {
        App app = getAppById(appId);
        User user = userService.getCurrentUser();
        AppFile appFile = app.getAppFile();
        Purchase purchase = purchaseService.getPurchaseByUserAndApp(user, app);

        if (!forceUpdate && !app.isNewerThan(purchase.getApp())) {
            throw new AppUpToDateException("Приложение актуально");
        }
        try {
            minioService.deleteFile(appFile.getFilePath());
            byte[] fileContent = minioService.downloadFile(appFile.getFilePath());
            appFile.setLastUpdated(LocalDateTime.now());
            purchaseRepository.save(purchase);
            return fileContent;
        } catch (Exception e) {
            throw new AppDownloadException("Не удалось скачать файл приложения", e);
        }
    }

    public void deleteApp(UUID appId) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(appId);
        if(!app.getAuthor().getEmail().equals(currentUser.getUsername()) && !(currentUser.getRole().equals(Role.MODERATOR) || currentUser.getRole().equals(Role.ADMIN))) {
            throw new AccessDeniedException("У вас нет права на удаление приложения");
        }

        minioService.deleteFile(app.getAppFile().getFilePath());
        appRepository.delete(app);
    }
}
