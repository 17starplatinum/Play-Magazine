package com.example.pmweb.repositories.data.app;

import com.example.pmweb.model.data.app.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {
}
