package com.example.pmcore.repositories.data.subscription;

import com.example.backend.model.data.subscriptions.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

}
