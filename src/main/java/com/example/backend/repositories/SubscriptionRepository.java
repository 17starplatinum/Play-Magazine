package com.example.backend.repositories;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUser(User user);

    Optional<Subscription> findByIdAndUser(UUID subscriptionId, User user);

    List<Subscription> findByActiveFalseAndEndDateAfter(LocalDate endDate);
}
