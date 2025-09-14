package com.example.pmcore.repositories.auth.custom;

import com.example.pmcore.model.auth.UserProfile;
import java.util.UUID;

public interface UserProfileRepositoryCustom {
    UserProfile save(UserProfile userProfile);
    UserProfile findById(UUID id);
}
