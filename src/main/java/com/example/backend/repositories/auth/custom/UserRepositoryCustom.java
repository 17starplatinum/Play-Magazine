package com.example.backend.repositories.auth.custom;

import com.example.backend.model.auth.RequestStatus;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryCustom {
    Optional<User> findByEmail(String username);
    User save(User user);
    Optional<User> findById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    void addAppToUser(UUID userId, UUID appId);
    List<User> findAll();
    void enableTwoFA(boolean enabled, String email);
    List<String> findAdminEmails();
    List<User> findByRequestStatus(RequestStatus status);
    void deleteById(UUID id);
    long count();
}
