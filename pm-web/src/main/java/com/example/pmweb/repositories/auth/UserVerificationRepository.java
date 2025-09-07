package com.example.pmweb.repositories.auth;

import com.example.pmweb.model.auth.UserVerification;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserVerificationRepository extends JpaRepository<UserVerification, UUID> {

    Optional<UserVerification> findByEmail(@NotNull String email);

    Optional<UserVerification> findByEmailAndId(@NotNull String email, @NotNull UUID uuid);
}