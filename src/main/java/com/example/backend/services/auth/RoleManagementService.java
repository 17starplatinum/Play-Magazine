package com.example.backend.services.auth;

import com.example.backend.exceptions.accepted.RequestPendingException;
import com.example.backend.exceptions.badcredentials.InvalidRequestException;
import com.example.backend.exceptions.conflict.AlreadyInRoleException;
import com.example.backend.exceptions.prerequisites.InvalidRoleAssignmentException;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserProfile;
import com.example.backend.repositories.auth.UserProfileRepository;
import com.example.backend.repositories.auth.UserRepository;
import com.example.backend.services.util.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.backend.model.auth.RequestStatus.*;
import static com.example.backend.model.auth.Role.ADMIN;
import static com.example.backend.model.auth.Role.DEVELOPER;

@Service
@RequiredArgsConstructor
public class RoleManagementService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public String requestRole(UUID userId, String requestedRole) {
        User user = userService.getById(userId);

        Role role = Role.valueOf(requestedRole.toUpperCase());

        if (user.getRole() == role) {
            throw new AlreadyInRoleException("Пользователь уже имеет роль, которого хочет приобрести");
        }

        if (user.getRequestStatus() == PENDING) {
            throw new RequestPendingException("Заявка уже в ожидании");
        }

        if (!adminExists()) {
            user.setRole(ADMIN);
            user.setRequestStatus(APPROVED);
            userRepository.save(user);
            notificationService.notifyAdminsAboutNewAuthorRequest(user);
            return "У системы нет администратора. Поэтому роль передается вам.";
        } else {
            user.setRequestStatus(PENDING);
            userRepository.save(user);
            notificationService.notifyAdminsAboutNewAuthorRequest(user);
            return "Заявка успешно подана. Дождитесь отмашки админа или модератора.";
        }

    }

    private boolean adminExists() {
        return userRepository.existsByRole(ADMIN);
    }

    @Transactional
    public void approveRequest(UUID userId) {
        User user = userService.getById(userId);

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Заявка пользователя не в состоянии обработки");
        }


        user.setRole(DEVELOPER);
        user.setRequestStatus(APPROVED);
        userRepository.save(user);

        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestApproval(user, cred.getName(), DEVELOPER.getName());
    }

    @Transactional
    public void rejectRequest(UUID userId, String role, String reason) {
        User user = userService.getById(userId);

        if (user.getRequestStatus() != PENDING) {
            throw new InvalidRequestException("Нельзя отклонить уже обработанную заявку");
        }

        user.setRequestStatus(REJECTED);
        userRepository.save(user);

        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestRejection(user, cred.getName(), role, reason);
    }

    @Transactional
    public void grantRole(UUID userId, String newRole) {
        User user = userService.getById(userId);

        Role roleToBe = Role.valueOf(newRole.toUpperCase());
        if (roleToBe.compare(DEVELOPER) < 0 && user.getRequestStatus() != APPROVED) {
            throw new InvalidRoleAssignmentException("Пользователь должен быть одобренным в качестве разработчика или выше");
        }
        if (user.getRole() == roleToBe) {
            throw new AlreadyInRoleException("Пользователь уже имеет роль, которого хочет приобрести");
        }
        user.setRole(roleToBe);
        userRepository.save(user);
        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestApproval(user, cred.getName(), newRole);
    }
}
