package com.example.pmweb.repositories.auth;

import com.example.pmweb.model.auth.User;

import java.util.Optional;

public interface UserFileRepository {
    Optional<User> findByUsernameFromFile(String username);
    void saveIntoFile(User user);
}
