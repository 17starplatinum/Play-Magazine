package com.example.backend.repositories;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUser(User user);

    Optional<Subscription> findByUserAndApp(User user, App app);
}
