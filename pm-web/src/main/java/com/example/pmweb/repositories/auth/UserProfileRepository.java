package com.example.pmweb.repositories.auth;

import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.auth.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findByUser(User user);
}
