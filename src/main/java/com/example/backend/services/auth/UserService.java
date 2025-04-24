package com.example.backend.services.auth;

import com.example.backend.dto.auth.SignUpRequest;
import com.example.backend.model.auth.*;
import com.example.backend.repositories.auth.UserBudgetRepository;
import com.example.backend.repositories.auth.UserProfileRepository;
import com.example.backend.repositories.auth.UserRepository;
import com.example.backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserBudgetRepository budgetRepository;
    private final UserProfileRepository profileRepository;
    private final JwtService jwtService;

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
        save(user);
        UserBudget userBudget = UserBudget.builder()
                .user(user)
                .spendingLimit(0D)
                .currentSpending(0D)
                .build();
        budgetRepository.save(userBudget);

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .name(signUpRequest.getName())
                .surname(signUpRequest.getSurname())
                .build();
        profileRepository.save(userProfile);
        userBudget.setUser(user);
        userProfile.setUser(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

    }

    public User getById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

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

    public List<User> findByRequestStatus(String requestStatus) {
        return userRepository.findByRequestStatus(RequestStatus.valueOf(requestStatus.toUpperCase()));
    }

    /**
     * Находит информацию о пользователя через его ID и соответствующего JWT-токена.
     * @param uuid ID
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
}