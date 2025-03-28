package com.example.backend.repositories;

import com.example.backend.model.data.App;
import com.example.backend.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    List<App> findByAvailable();
    List<App> findByAuthor(User author);
    Optional<App> findByIdAndAvailable(UUID id);
}
