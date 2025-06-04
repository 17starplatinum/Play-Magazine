package com.example.backend.repositories.auth;

import com.example.backend.model.auth.RequestStatus;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Nonnull
    Optional<User> findById(@Nonnull UUID id);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    @Query("SELECT u.email FROM User u WHERE u.role = 'ADMIN'")
    List<String> findAdminEmails();

    @Query("SELECT u FROM User u WHERE u.requestStatus = :requestStatus")
    List<User> findByRequestStatus(@Param("requestStatus") RequestStatus requestStatus);

    @Modifying
    @Query("UPDATE User u SET u.enableTwoFA = :enabled WHERE u.email = :email")
    void enableTwoFA(@Param("enabled") boolean enabled, @Param("email") String email);

    @Modifying
    @Query(value = "INSERT INTO user_app_downloads (user_id, app_id) VALUES (:userId, :appId)", nativeQuery = true)
    void addAppToUser(@Param("userId") UUID userId, @Param("appId") UUID appId);
}
