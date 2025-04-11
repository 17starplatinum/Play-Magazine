package com.example.backend.services;

import com.example.backend.model.data.UserVerification;
import com.example.backend.repositories.UserVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final UserVerificationRepository userVerificationRepository;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration CODE_EXPIRY = Duration.ofMinutes(1);

    public boolean isValidVerificationCode(UUID verificationId, String code, String email) {
        UserVerification userVerification = userVerificationRepository
                .findByEmailAndId(email, verificationId)
                .orElse(null);

        if (userVerification == null || !userVerification.isEnable())
            throw new IllegalArgumentException("Code expired! Please get new verification code!");

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
                throw new IllegalArgumentException("Code expired! Please get new verification code!");
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
        throw new IllegalArgumentException("Unknown verification code!");
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
