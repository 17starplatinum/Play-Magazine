package com.example.backend.services.auth;

import com.example.backend.model.auth.UserVerification;
import com.example.backend.repositories.auth.UserVerificationRepository;
import com.example.backend.services.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class
UserVerificationService {

    private final UserVerificationRepository userVerificationRepository;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration CODE_EXPIRY = Duration.ofMinutes(1);

    public boolean isValidVerificationCode(UUID verificationId, String code, String email) {
        UserVerification userVerification = userVerificationRepository
                .findByEmailAndId(email, verificationId)
                .orElse(null);

        if (userVerification == null || !userVerification.isEnable())
            throw new IllegalArgumentException("Срок отправленного кода истекло! Подавайте запрос на новый код ещё раз.");

        Instant creationInstant = Instant.ofEpochMilli(userVerification.getCreationTime());
        if (Instant.now().isAfter(creationInstant.plus(CODE_EXPIRY))) {
            disableVerification(verificationId);
            return false;
        }

        if (userVerification.getVerificationCode().equals(code)) {
            disableVerification(verificationId);
            return true;
        } else {
            int attempts = incrementFailedAttempts(verificationId);

            if (attempts >= MAX_ATTEMPTS) {
                disableVerification(verificationId);
                throw new IllegalArgumentException("Срок отправленного кода истекло! Подавайте запрос на новый код ещё раз.");
            }

            return false;
        }
    }

    public UserVerification createUserVerification(String email) {
        UserVerification userVerification = UserVerification.builder()
                .email(email)
                .build();

        emailService.sendVerificationEmail(email, userVerification.getVerificationCode());
        userVerification = userVerificationRepository.save(userVerification);
        return userVerification;
    }

    public Optional<UUID> getVerificationIdByEmail(String email) {
        Optional<UserVerification> userVerification = userVerificationRepository.findByEmail(email);
        return userVerification.map(UserVerification::getId);
    }

    private int incrementFailedAttempts(UUID verificationId) {
        UserVerification userVerification = userVerificationRepository.findById(verificationId).orElse(null);
        if (userVerification != null) {
            userVerification.setFailedAttempts(userVerification.getFailedAttempts() + 1);
            userVerificationRepository.save(userVerification);
            return userVerification.getFailedAttempts();
        }
        throw new IllegalArgumentException("Verification code is invalid!");
    }

    private void disableVerification(UUID verificationId) {
        UserVerification userVerification = userVerificationRepository.findById(verificationId).orElse(null);
        if (userVerification != null) {
            userVerification.setEnable(false);
            userVerificationRepository.save(userVerification);
        }
    }

    public void deleteVerificationCode(UUID oldCodeId) {
        userVerificationRepository.deleteById(oldCodeId);
    }
}
