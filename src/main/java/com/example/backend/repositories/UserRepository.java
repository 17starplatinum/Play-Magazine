package com.example.backend.repositories;

import com.example.backend.model.auth.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Nonnull
    Optional<User> findById(@Nonnull UUID id);

    boolean existsByEmail(String email);
}
