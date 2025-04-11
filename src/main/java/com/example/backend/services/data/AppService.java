package com.example.backend.services.data;

import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.dto.data.app.AppDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.prerequisites.InvalidApplicationConfigException;
import com.example.backend.model.data.App;
import com.example.backend.repositories.AppRepository;
import com.example.backend.repositories.PurchaseRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.util.FileUtils;
import com.example.backend.services.util.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private static final String APP_NOT_FOUND = "Приложение не найдено";
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final ReviewService reviewService;

    public AppDownloadResponse prepareAppDownload(UUID appId, UserDetails currentUser) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND, new RuntimeException()));

        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        Purchase purchase = purchaseRepository.findByUserAndApp(user, app)
                .orElseThrow(() -> new AppNotPurchasedException("Приложение не куплено", new RuntimeException()));

        boolean updateAvailable = app.isNewerVersion(purchase.getInstalledVersion());
        return AppDownloadResponse.builder()
                .appId(app.getId())
                .name(app.getName())
                .currentVersion(purchase.getInstalledVersion())
                .availableVersion(app.getVersion())
                .updateAvailable(updateAvailable)
                .fileSize(app.getFileSize())
                .fileHash(app.getFileHash())
                .build();
    }

    public AppCompatibilityResponse checkCompatibility(UUID appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND, new RuntimeException()));
        List<String> compatibilityIssues = new ArrayList<>();

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        GlobalMemory memory = si.getHardware().getMemory();
        List<HWDiskStore> diskStores = si.getHardware().getDiskStores();



        if(app.getMinRamMb() > memory.getTotal()){
            compatibilityIssues.add(String.format(
                    "Не хватает памяти ОЗУ: требуется %dМб, а доступно только %dМб",
                    app.getMinRamMb(), memory.getTotal()
            ));
        }

        boolean enoughDiskSpace = false;
        for(HWDiskStore diskStore : diskStores) {
            if(app.getMinStorageMb() < diskStore.getSize()){
                enoughDiskSpace = true;
                break;
            }
        }

        if(!enoughDiskSpace){
            for(HWDiskStore diskStore : diskStores) {
                compatibilityIssues.add(String.format(
                        "Не хватает пространства на диске: требуется %dМб, а доступно только %dМб",
                        app.getMinStorageMb(), diskStore.getSize()
                ));
            }
        }



        if(!app.getOsRequirements().matches(os.getFamily())) {
            compatibilityIssues.add(String.format(
                    "ОС не поддерживается приложением: целевая платформа: %s, но на устройстве %s",
                    app.getOsRequirements(), os.getFamily()
            ));
        }

        return AppCompatibilityResponse.builder()
                .compatible(compatibilityIssues.isEmpty())
                .issues(compatibilityIssues)
                .averageRating(reviewService.getAverageRating(appId))
                .build();
    }

    public List<App> getAllAvailableApps() {
        return appRepository.findByAvailableTrue();
    }

    public App getAppById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND, new RuntimeException()));
    }

    public App createApp(AppDto appDto, UserDetails currentUser, MultipartFile file) {
        User author = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        App app = App.builder()
                .name(appDto.getName())
                .author(author)
                .price(appDto.getPrice())
                .isSubscription(appDto.isSubscription())
                .subscriptionPrice(appDto.getSubscriptionPrice())
                .description(appDto.getDescription())
                .available(true)
                .releaseDate(LocalDate.now())
                .version(1.0f)
                .filePath(filePath)
                .build();
        if (appDto.isSubscription() && (appDto.getSubscriptionPrice() == null || appDto.getSubscriptionPrice() == 0)) {
            throw new InvalidApplicationConfigException("У приложении с подписками должна быть цена", new RuntimeException());
        }
        return appRepository.save(app);
    }

    public App updateApp(UUID appId, MultipartFile file, UserDetails currentUser, float versionBump) {
        App app = getAppById(appId);
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найдено", new RuntimeException()));
        if (!app.getAuthor().equals(user)) {
            throw new InvalidDataAccessApiUsageException("Вы не являетесь создателем приложения");
        }

        try {
            minioService.deleteFile(app.getFilePath());
            String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());
            byte[] fileContent = file.getBytes();

            app.setVersion(app.getVersion() + versionBump);
            app.setFileHash(filePath);
            app.setFileSize(file.getSize());
            app.setFileHash(FileUtils.calculateFileHash(fileContent));
            app.setLastUpdated(LocalDateTime.now());

            return appRepository.save(app);
        } catch (IOException e) {
            throw new AppUpdateException("Не удалось читать содержимое файла", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Не удалось вычислить хэш файла", e);
        }
    }

    public byte[] downloadAppFile(UUID appId, UserDetails currentUser, boolean forceUpdate) {
        App app = getAppById(appId);
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найдено", new RuntimeException()));

        Purchase purchase = purchaseRepository.findByUserAndApp(user, app)
                .orElseThrow(() -> new AppNotPurchasedException("Приложение не куплено", new RuntimeException()));

        if (!forceUpdate && !app.isNewerVersion(purchase.getInstalledVersion())) {
            throw new AppUpToDateException("Приложение актуально", new RuntimeException());
        }
        try {
            byte[] fileContent = minioService.downloadFile(app.getFilePath());
            purchase.setInstalledVersion(app.getVersion());
            purchase.setLastUpdated(LocalDateTime.now());
            purchaseRepository.save(purchase);
            return fileContent;
        } catch (Exception e) {
            throw new AppDownloadException("Не удалось скачать файл приложения", e);
        }
    }

    public void deleteApp(UUID appId, UserDetails currentUser) {
        App app = getAppById(appId);
        if(!app.getAuthor().getEmail().equals(currentUser.getUsername())) {
            throw new InvalidDataAccessApiUsageException("Вы не являетесь автором приложения");
        }

        minioService.deleteFile(app.getFilePath());
        appRepository.delete(app);
    }
}
