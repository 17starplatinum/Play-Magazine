package com.example.backend.repositories.auth.custom;

import com.example.backend.model.auth.UserProfile;
import java.util.UUID;

public interface UserProfileRepositoryCustom {
    UserProfile save(UserProfile userProfile);
    UserProfile findById(UUID id);
}
