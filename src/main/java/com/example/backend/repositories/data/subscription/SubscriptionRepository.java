package com.example.backend.repositories.data.subscription;

import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    void deleteAllBySubscribedUser(Set<UserSubscription> userSubscriptions);
}
