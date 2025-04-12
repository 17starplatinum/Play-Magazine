package com.example.backend.repositories.data.subscription;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.SubscriptionInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUser(User user);

    Optional<Subscription> findByIdAndUser(UUID subscriptionId, User user);

    void deleteBySubscriptionInfoIn(List<SubscriptionInfo> subscriptionInfos);
}
