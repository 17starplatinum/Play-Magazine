package com.example.pmweb.services.auth;

import com.example.pmweb.exceptions.accepted.RequestPendingException;
import com.example.pmweb.exceptions.badcredentials.InvalidRequestException;
import com.example.pmweb.exceptions.conflict.AlreadyInRoleException;
import com.example.pmweb.exceptions.prerequisites.InvalidRoleAssignmentException;
import com.example.pmweb.model.auth.Role;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.auth.UserProfile;
import com.example.pmweb.repositories.auth.UserProfileRepository;
import com.example.pmweb.repositories.auth.UserRepository;
import com.example.pmweb.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static com.example.pmweb.model.auth.RequestStatus.*;
import static com.example.pmweb.model.auth.Role.ADMIN;
import static com.example.pmweb.model.auth.Role.DEVELOPER;

@Service
@RequiredArgsConstructor
public class RoleManagementService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;

    public String requestRole(String requestedRole) {
        User user = userService.getCurrentUser();
        TransactionStatus transaction = transactionManager.getTransaction(definition);

        Role role = Role.valueOf(requestedRole.toUpperCase());

        if (user.getRole() == role) {
            transactionManager.rollback(transaction);
            throw new AlreadyInRoleException("Пользователь уже имеет роль, которого хочет приобрести");
        }

        if (user.getRequestStatus() == PENDING) {
            transactionManager.rollback(transaction);
            throw new RequestPendingException("Заявка уже в ожидании");
        }

        if (!adminExists()) {
            user.setRole(ADMIN);
            user.setRequestStatus(APPROVED);
            userService.save(user);
            transactionManager.commit(transaction);
            return "У системы нет администратора. Поэтому роль передается вам.";
        } else {
            user.setRequestStatus(PENDING);
            userService.save(user);
            transactionManager.commit(transaction);
            return "Дождитесь отмашки админа или модератора.";
        }
    }

    private boolean adminExists() {
        return userRepository.existsByRole(ADMIN);
    }

    public void approveRequest(String username) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRequestStatus() != PENDING) {
            transactionManager.rollback(transaction);
            throw new InvalidRequestException("Заявка пользователя не в состоянии обработки");
        }

        user.setRole(DEVELOPER);
        user.setRequestStatus(APPROVED);
        user = userService.save(user);
        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestApproval(user, cred.getName(), DEVELOPER.toString());
        transactionManager.commit(transaction);
    }

    public void rejectRequest(String username, String role, String reason) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRequestStatus() != PENDING) {
            transactionManager.rollback(transaction);
            throw new InvalidRequestException("Нельзя отклонить уже обработанную заявку");
        }

        user.setRequestStatus(REJECTED);
        user = userService.save(user);
        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestRejection(user, cred.getName(), reason, role);
        transactionManager.commit(transaction);
    }

    public void grantRole(String username, String newRole) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Role roleToBe = Role.valueOf(newRole.toUpperCase());
        if (roleToBe.compare(DEVELOPER) < 0 && user.getRequestStatus() != APPROVED) {
            transactionManager.rollback(transaction);
            throw new InvalidRoleAssignmentException("Пользователь должен быть одобренным в качестве разработчика или выше");
        }
        if (user.getRole() == roleToBe) {
            transactionManager.rollback(transaction);
            throw new AlreadyInRoleException("Пользователь уже имеет роль, которого хочет приобрести");
        }
        user.setRole(roleToBe);
        user.setRequestStatus(NOT_REQUESTED);
        user = userService.save(user);
        UserProfile cred = userProfileRepository.findByUser(userService.getCurrentUser());
        notificationService.notifyUserAboutAuthorRequestApproval(user, cred.getName(), newRole);
        transactionManager.commit(transaction);
    }
}
