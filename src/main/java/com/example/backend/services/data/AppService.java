package com.example.backend.services.data;

import com.example.backend.dto.util.AppCompatibilityResponse;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OperatingSystem;
import com.example.backend.dto.data.AppDto;
import com.example.backend.exceptions.AppNotFoundException;
import com.example.backend.exceptions.AppNotPurchasedException;
import com.example.backend.exceptions.UserNotFoundException;
import com.example.backend.model.data.App;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.AppRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.util.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;

    public AppCompatibilityResponse checkCompatibility(UUID appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));
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
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));
    }

    public App createApp(AppDto appDto, UserDetails currentUser, MultipartFile file) {
        User author = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        String filePath = minioService.uploadFile(file, UUID.randomUUID().toString());

        App app = App.builder()
                .name(appDto.getName())
                .author(author)
                .price(appDto.getPrice())
                .description(appDto.getDescription())
                .available(true)
                .releaseDate(LocalDate.now())
                .version(1.0f)
                .filePath(filePath)
                .build();

        return appRepository.save(app);
    }

    public byte[] downloadAppFile(UUID appId, UserDetails currentUser) {
        App app = getAppById(appId);
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найдено", new RuntimeException()));

        if(purchaseService.hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("Приложение не куплено", new RuntimeException());
        }

        return minioService.downloadFile(app.getFilePath());
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
