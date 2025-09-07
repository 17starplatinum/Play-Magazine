package com.example.pmweb.repositories.data.subscription;

import com.example.pmweb.model.data.subscriptions.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

}
