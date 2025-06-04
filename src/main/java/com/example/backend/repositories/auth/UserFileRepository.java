package com.example.backend.repositories.auth;

import com.example.backend.model.auth.User;

import java.util.Optional;

public interface UserFileRepository {
    Optional<User> findByUsernameFromFile(String username);
    void saveIntoFile(User user);
}
