package com.example.backend.services.auth;

import com.example.backend.exceptions.accepted.RequestPendingException;
import com.example.backend.exceptions.badcredentials.InvalidRequestException;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.exceptions.prerequisites.AlreadyDeveloperException;
import com.example.backend.exceptions.prerequisites.InvalidRoleAssignmentException;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.util.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.backend.model.auth.RequestStatus.*;
import static com.example.backend.model.auth.Role.DEVELOPER;

@Service
@RequiredArgsConstructor
public class RoleManagementService {
    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void requestRole(UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, new RuntimeException()));

        if (user.getRole() == DEVELOPER) {
            throw new AlreadyDeveloperException("Пользователь уже является разработчиком", new RuntimeException());
        }

        if (user.getRequestStatus() == PENDING) {
            throw new RequestPendingException("Заявка уже в ожидании", new RuntimeException());
        }
        user.setRequestStatus(PENDING);
        userRepository.save(user);

        notificationService.notifyAdminsAboutNewAuthorRequest(user);
    }

    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public void approveRequest(UUID userId, String approvedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, new RuntimeException()));

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Заявка пользователя не обрабатывается", new RuntimeException());
        }

        user.setRole(DEVELOPER);
        user.setRequestStatus(APPROVED);
        userRepository.save(user);

        notificationService.notifyUserAboutAuthorRequestApproval(user, approvedBy);
    }

    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public void rejectRequest(UUID userId, String rejectedBy, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, new RuntimeException()));

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Нельзя отклонить уже обработанную заявку", new RuntimeException());
        }

        user.setRequestStatus(REJECTED);
        userRepository.save(user);

        notificationService.notifyUserAboutAuthorRequestRejection(user, rejectedBy, reason);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void grantRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, new RuntimeException()));

        if (role == DEVELOPER && user.getRequestStatus() != APPROVED) {
            throw new InvalidRoleAssignmentException("Пользователь должен быть одобренным в качестве разработчика", new RuntimeException());
        }

        user.setRole(role);
        userRepository.save(user);
    }
}
