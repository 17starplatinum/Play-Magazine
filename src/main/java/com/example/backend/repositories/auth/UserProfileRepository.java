package com.example.backend.repositories.auth;

import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findByUser(User user);
}
