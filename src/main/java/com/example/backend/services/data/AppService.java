package com.example.backend.services.data;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final PurchaseService purchaseService;

    public List<App> getAllAvailableApps() {
        return appRepository.findByAvailable();
    }

    public App getAppById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));
    }

    public App createApp(AppDto appDto, UserPrincipal currentUser, MultipartFile file) {
        User author = userRepository.findById(currentUser.getId())
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

    public byte[] downloadAppFile(UUID appId, UserPrincipal currentUser) {
        App app = getAppById(appId);
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найдено", new RuntimeException()));

        if(!purchaseService.hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("Приложение не куплено", new RuntimeException());
        }

        return minioService.downloadFile(app.getFilePath());
    }

    public void deleteApp(UUID appId, UserPrincipal currentUser) throws AccessDeniedException {
        App app = getAppById(appId);
        if(!app.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не являетесь автором приложения");
        }

        minioService.deleteFile(app.getFilePath());
        appRepository.delete(app);
    }
}
