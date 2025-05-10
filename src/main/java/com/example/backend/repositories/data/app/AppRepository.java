package com.example.backend.repositories.data.app;

import com.example.backend.model.data.app.App;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    @EntityGraph(attributePaths = {"downloadedApps"})
    App findByName(String name);

    @Query(value = "SELECT a FROM App a LEFT JOIN a.subscriptions o WHERE (:id == a.subscriptions.subscriptionInfo.id)")
    Optional<App> findByIdAndSubscriptionId(String name, UUID id);

    @Query(value = "SELECT * FROM apps a ORDER BY a.release_date DESC LIMIT :limit", nativeQuery = true)
    List<App> findAppsLimit(@Param("limit") int limit);
}
