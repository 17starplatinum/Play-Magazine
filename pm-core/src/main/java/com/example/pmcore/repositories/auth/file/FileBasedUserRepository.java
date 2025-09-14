package com.example.pmcore.repositories.auth.file;

import com.example.pmcore.dto.auth.file.UserDatabaseFileDto;
import com.example.pmcore.dto.auth.file.UserFileDto;
import com.example.pmcore.model.auth.RequestStatus;
import com.example.pmcore.model.auth.Role;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.data.subscriptions.Subscription;
import com.example.pmcore.repositories.auth.custom.UserRepositoryCustom;
import com.example.pmcore.repositories.data.app.UserAppDownloadRepository;
import com.example.pmcore.repositories.data.subscription.UserSubscriptionRepository;
import com.example.pmcore.services.util.XMLParser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository("fileBasedUserRepository")
public class FileBasedUserRepository implements UserRepositoryCustom {
    private final XMLParser xmlParser;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserAppDownloadRepository userAppDownloadRepository;
    private final String xmlPath = "users.xml";
    private volatile UserDatabaseFileDto cachedDatabase;

    public FileBasedUserRepository(XMLParser xmlParser, UserSubscriptionRepository userSubscriptionRepository,  UserAppDownloadRepository userAppDownloadRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userAppDownloadRepository = userAppDownloadRepository;
        this.xmlParser = xmlParser;
        this.cachedDatabase = loadDatabase();
    }

    private synchronized UserDatabaseFileDto loadDatabase() {
        try {
            UserDatabaseFileDto db = xmlParser.getEntity(xmlPath, UserDatabaseFileDto.class);
            return db != null ? db : new UserDatabaseFileDto();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load user database from XML", e);
        }
    }

    private synchronized void saveDatabase(UserDatabaseFileDto db) {
        try {
            xmlParser.saveEntity(db, xmlPath);
            this.cachedDatabase = db;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user database to XML", e);
        }
    }

    private UserFileDto toDto(User user) {
        Set<UUID> subscriptionIds = userSubscriptionRepository.findSubscriptionsByUserId(user.getId())
                .stream()
                .map(Subscription::getId)
                .collect(Collectors.toSet());

        Set<UUID> downloadedAppIds = userAppDownloadRepository.findAppIdsByIdUserId(user.getId())
                .stream()
                .map(u -> u.getApp().getId())
                .collect(Collectors.toSet());
        return UserFileDto.builder()
                .id(user.getId())
                .password(user.getPassword())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enableTwoFA(user.isEnableTwoFA())
                .requestStatus(user.getRequestStatus().name())
                .userBudgetId(user.getUserBudgetId() != null ? user.getUserBudgetId() : null)
                .userProfileId(user.getUserProfileId() !=  null ? user.getUserProfileId() : null)
                .userVerificationId(null)
                .downloadedAppIds(downloadedAppIds)
                .userSubscriptionIds(subscriptionIds)
                .build();
    }

    private User fromDto(UserFileDto dto) {
        return User.builder()
                .id(dto.getId())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .role(Role.valueOf(dto.getRole()))
                .enableTwoFA(dto.isEnableTwoFA())
                .userBudgetId(dto.getUserBudgetId())
                .userProfileId(dto.getUserProfileId())
                .requestStatus(RequestStatus.valueOf(dto.getRequestStatus()))
                .build();
    }

    @Override
    public Optional<User> findById(UUID id) {
        UserFileDto dto = cachedDatabase.findUserById(id);
        return dto != null ? Optional.of(fromDto(dto)) : Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return cachedDatabase.getUsers().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .map(this::fromDto);
    }

    @Override
    public User save(User user) {
        UserFileDto dto = toDto(user);
        UserDatabaseFileDto db = loadDatabase();

        for (int i = 0; i < db.getUsers().size(); i++) {
            if (db.getUsers().get(i).getId().equals(dto.getId())) {
                db.getUsers().set(i, dto);
                saveDatabase(db);
                return user;
            }
        }

        db.getUsers().add(dto);
        saveDatabase(db);
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return cachedDatabase.getUsers().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    @Override
    public boolean existsByRole(Role role) {
        return cachedDatabase.getUsers().stream().anyMatch(u -> u.getRole().equals(role.name()));
    }

    @Override
    public List<String> findAdminEmails() {
        return cachedDatabase.getUsers().stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .map(UserFileDto::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByRequestStatus(RequestStatus status) {
        return cachedDatabase.getUsers().stream()
                .filter(u -> u.getRequestStatus().equals(status.name()))
                .map(this::fromDto)
                .collect(Collectors.toList());
    }

    @Override
    public void enableTwoFA(boolean enabled, String email) {
        UserDatabaseFileDto db = loadDatabase();
        for (UserFileDto user : db.getUsers()) {
            if (user.getEmail().equals(email)) {
                user.setEnableTwoFA(enabled);
                saveDatabase(db);
                return;
            }
        }
    }

    @Override
    public void addAppToUser(UUID userId, UUID appId) {
        UserDatabaseFileDto db = loadDatabase();
        UserFileDto user = db.findUserById(userId);
        if (user != null) {
            user.getDownloadedAppIds().add(appId);
            saveDatabase(db);
        }
    }

    @Override
    public List<User> findAll() {
        return cachedDatabase.getUsers().stream().map(this::fromDto).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        UserDatabaseFileDto db = loadDatabase();
        db.getUsers().removeIf(u -> u.getId().equals(id));
        saveDatabase(db);
    }

    @Override
    public long count() {
        return cachedDatabase.getUsers().size();
    }
}
