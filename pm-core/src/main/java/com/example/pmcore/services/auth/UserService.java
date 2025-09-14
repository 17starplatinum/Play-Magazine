package com.example.pmcore.services.auth;

import com.example.backend.dto.auth.RoleChangeRequestDto;
import com.example.backend.dto.auth.SignUpRequest;
import com.example.backend.dto.auth.StatusResponse;
import com.example.backend.dto.auth.rolestatus.AdminRequestStatusHandler;
import com.example.backend.repositories.auth.file.FileBasedUserBudgetRepository;
import com.example.backend.repositories.auth.file.FileBasedUserProfileRepository;
import com.example.backend.repositories.auth.file.FileBasedUserRepository;
import com.example.backend.security.jwt.JwtService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final FileBasedUserBudgetRepository budgetRepository;
    private final FileBasedUserProfileRepository profileRepository;
    private final JwtService jwtService;
    private final Map<String, AdminRequestStatusHandler> statusHandlerMap;
    private final FileBasedUserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;
    @Resource
    private UserService userServiceResource;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return userRepository.save(user);
    }


    /**
     * Создание пользователя
     */
    public void create(User user, SignUpRequest signUpRequest) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        if (userRepository.existsByEmail(user.getEmail())) {
            transactionManager.rollback(transaction);
            throw new BadCredentialsException("User with this email already existed!");
        }
        UserBudget userBudget = UserBudget.builder()
                .id(UUID.randomUUID())
                .spendingLimit(null)
                .currentSpending(0D)
                .build();
        userBudget = budgetRepository.save(userBudget);
        user.setUserBudgetId(userBudget.getId());

        UserProfile userProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .name(signUpRequest.getName())
                .surname(signUpRequest.getSurname())
                .build();
        userProfile = profileRepository.save(userProfile);
        user.setUserProfileId(userProfile.getId());
        userServiceResource.save(user);

        transactionManager.commit(transaction);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow();

    }

    public User getById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public UUID getCurrentUserId() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username).getId();
    }

    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     *
     * @deprecated так как у нас теперь есть более-менее внятная система ролей, этот метод больше не является актуальным.
     */
    @Deprecated(forRemoval = true)
    public void getAdmin() {
        var user = getCurrentUser();
        user.setRole(Role.ADMIN);
        userServiceResource.save(user);
    }

    public boolean checkValidUser(UUID uuid, String token) {
        token = token.substring(7);
        String email = jwtService.extractUserName(token);
        return getByUsername(email).getId().equals(uuid);
    }

    public List<RoleChangeRequestDto> findByRequestStatus(String requestStatus) {
        return userRepository.findByRequestStatus(RequestStatus.valueOf(requestStatus)).stream().map(this::convertToRoleRequestDto).toList();
    }

    public StatusResponse getAdminRequestStatus() {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = getCurrentUser();
        String userRequestStatus = user.getRequestStatus().name();
        AdminRequestStatusHandler adminRequestStatusHandler = statusHandlerMap.get(userRequestStatus);
        if (adminRequestStatusHandler == null) {
            transactionManager.rollback(transaction);
            throw new IllegalStateException("Обработчик для статуса '" + user.getRequestStatus().toString() + "' не найден");
        }
        transactionManager.commit(transaction);
        return StatusResponse.builder()
                .requestStatus(userRequestStatus)
                .role(String.valueOf(user.getRole()))
                .build();
    }

    private RoleChangeRequestDto convertToRoleRequestDto(User user) {
        return RoleChangeRequestDto.builder()
                .email(user.getUsername())
                .role(user.getRole().toString())
                .requestStatus(user.getRequestStatus().toString())
                .build();
    }

    /**
     * Находит информацию о пользователя через его ID и соответствующего JWT-токена.
     *
     * @param uuid  ID
     * @param token JWT-токен
     * @return пользователь
     * @deprecated так как были реализованы репозиторий, этот метод больше не является актуальным.
     */
    @Deprecated(forRemoval = true)
    public User getUserInfoById(UUID uuid, String token) {
        if (checkValidUser(uuid, token))
            return getById(uuid);
        throw new IllegalArgumentException("Something went wrong!");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Пользователь не найден!")
        );
    }
}