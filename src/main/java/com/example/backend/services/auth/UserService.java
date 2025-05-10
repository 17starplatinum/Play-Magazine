package com.example.backend.services.auth;

import com.example.backend.dto.auth.RoleChangeRequestDto;
import com.example.backend.dto.auth.SignUpRequest;
import com.example.backend.dto.auth.rolestatus.AdminRequestStatusHandler;
import com.example.backend.model.auth.*;
import com.example.backend.repositories.auth.UserBudgetRepository;
import com.example.backend.repositories.auth.UserProfileRepository;
import com.example.backend.repositories.auth.UserRepository;
import com.example.backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final UserRepository userRepository;
    private final UserBudgetRepository budgetRepository;
    private final UserProfileRepository profileRepository;
    private final JwtService jwtService;
    private Map<String, AdminRequestStatusHandler> statusHandlerMap;


    @Autowired
    public void setStatusHandlerMap(Map<String, AdminRequestStatusHandler> statusHandlerMap) {
        this.statusHandlerMap = statusHandlerMap;
    }

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
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadCredentialsException("Пользователь с таким email уже существует");
        }
        UserBudget userBudget = UserBudget.builder()
                .spendingLimit(null)
                .currentSpending(0D)
                .build();
        budgetRepository.save(userBudget);
        user.setUserBudget(userBudget);
        save(user);

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .name(signUpRequest.getName())
                .surname(signUpRequest.getSurname())
                .build();
        profileRepository.save(userProfile);

        userProfile.setUser(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

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
        save(user);
    }

    public boolean checkValidUser(UUID uuid, String token) {
        token = token.substring(7);
        String email = jwtService.extractUserName(token);
        return getByUsername(email).getId().equals(uuid);
    }

    public List<RoleChangeRequestDto> findByRequestStatus(String requestStatus) {
        return userRepository.findByRequestStatus(RequestStatus.valueOf(requestStatus)).stream().map(this::convertToRoleRequestDto).toList();
    }

    @Transactional(readOnly = true)
    public String getAdminRequestStatus(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        String userRole = user.getRole().name();
        AdminRequestStatusHandler adminRequestStatusHandler = statusHandlerMap.get(userRole);
        if (adminRequestStatusHandler == null)
            throw new IllegalStateException("Обработчик для статуса '" + user.getEmail() + "' не найден");

        return adminRequestStatusHandler.getStatusMessage();
    }

    private RoleChangeRequestDto convertToRoleRequestDto(User user) {
        return RoleChangeRequestDto.builder()
                .userId(user.getId())
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
        return userRepository.findUserByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException("Пользователь не найден!")
        );
    }
}