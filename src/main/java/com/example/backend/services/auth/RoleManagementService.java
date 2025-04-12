package com.example.backend.services.auth;

import com.example.backend.exceptions.accepted.RequestPendingException;
import com.example.backend.exceptions.badcredentials.InvalidRequestException;
import com.example.backend.exceptions.prerequisites.AlreadyInRoleException;
import com.example.backend.exceptions.prerequisites.InvalidRoleAssignmentException;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.auth.UserRepository;
import com.example.backend.services.util.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.backend.model.auth.RequestStatus.*;
import static com.example.backend.model.auth.Role.DEVELOPER;

@Service
@RequiredArgsConstructor
public class RoleManagementService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional
    public void requestRole(String requestedRole) {
        User user = userService.getCurrentUser();

        Role role = Role.valueOf(requestedRole.toUpperCase());

        if (user.getRole() == role) {
            throw new AlreadyInRoleException("Пользователь уже имеет роль, которого хочет приобрести");
        }

        if (user.getRequestStatus() == PENDING) {
            throw new RequestPendingException("Заявка уже в ожидании");
        }

        user.setRequestStatus(PENDING);
        userRepository.save(user);

        notificationService.notifyAdminsAboutNewAuthorRequest(user);
    }

    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public void approveRequest(UUID userId, String approvedBy) {
        User user = userService.getCurrentUser();

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Заявка пользователя не обрабатывается");
        }

        user.setRole(DEVELOPER);
        user.setRequestStatus(APPROVED);
        userRepository.save(user);

        notificationService.notifyUserAboutAuthorRequestApproval(user, approvedBy);
    }

    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public void rejectRequest(UUID userId, String rejectedBy, String reason) {
        User user = userService.getCurrentUser();

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Нельзя отклонить уже обработанную заявку");
        }

        user.setRequestStatus(REJECTED);
        userRepository.save(user);

        notificationService.notifyUserAboutAuthorRequestRejection(user, rejectedBy, reason);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void grantRole(UUID userId, String newRole) {
        User user = userService.getCurrentUser();

        Role roleToBe = Role.valueOf(newRole.toUpperCase());
        if (roleToBe.compare(DEVELOPER) < 0 && user.getRequestStatus() != APPROVED) {
            throw new InvalidRoleAssignmentException("Пользователь должен быть одобренным в качестве разработчика или выше");
        }

        user.setRole(roleToBe);
        userRepository.save(user);
    }
}
