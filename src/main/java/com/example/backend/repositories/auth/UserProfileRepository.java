package com.example.backend.repositories.auth;

import com.example.backend.model.auth.UserProfile;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaAttributeConverter<UserProfile, UUID> {
}
