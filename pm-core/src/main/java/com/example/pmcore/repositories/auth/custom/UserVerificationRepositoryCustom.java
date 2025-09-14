package com.example.pmcore.repositories.auth.custom;

import com.example.backend.model.auth.UserVerification;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface UserVerificationRepositoryCustom {
    UserVerification save(UserVerification userVerification);
    Optional<UserVerification> findById(@NotNull UUID id);
    Optional<UserVerification> findByEmail(@NotNull String email);
    Optional<UserVerification> findByEmailAndId(@NotNull String email, @NotNull UUID uuid);
    void deleteById(@NotNull UUID id);
}
