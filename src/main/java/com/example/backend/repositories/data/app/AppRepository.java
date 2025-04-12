package com.example.backend.repositories.data.app;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    App findByName(String name);
}
