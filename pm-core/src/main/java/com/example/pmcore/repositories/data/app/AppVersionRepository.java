package com.example.pmcore.repositories.data.app;

import com.example.backend.model.data.app.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {
}
