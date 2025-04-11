package com.example.backend.repositories;

import com.example.backend.model.auth.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @NotNull
    Optional<User> findById(@NotNull UUID id);

    boolean existsByEmail(String email);
}
