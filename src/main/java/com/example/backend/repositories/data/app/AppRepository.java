package com.example.backend.repositories.data.app;

import com.example.backend.model.data.app.App;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    @EntityGraph(attributePaths = {"downloadedApps"})
    App findByName(String name);
}
