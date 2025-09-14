package com.example.backend.repositories.data.subscription;

import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import com.example.backend.model.data.subscriptions.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.id.userId = :userId")
    List<Subscription> findSubscriptionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT us FROM UserSubscription us WHERE us.subscription.id = :subscriptionId")
    UserSubscription findUserSubscriptionBySubscriptionId(@Param("subscriptionId") UUID subscriptionId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.id.userId = :userId AND us.subscription.id = :subscriptionId")
    Optional<Subscription> findSubscriptionByIdAndUser(@Param("subscriptionId") UUID subscriptionId, @Param("userId") UUID userId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.subscription.id = :subscriptionId AND us.subscription.app.id = :appId")
    Optional<UserSubscription> findBySubscriptionAndApp(@Param("subscriptionId") UUID subscriptionId, @Param("appId") UUID appId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.subscription.app.id = :appId")
    List<Subscription> findByApp(@Param("appId") UUID appId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.id.userId = :userId AND us.subscription.app.id = :appId")
    List<Subscription> findSubscriptionsByUserAndApp(@Param("userId") UUID userId, @Param("appId") UUID appId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSubscription us WHERE us.subscription = :subscription")
    void deleteBySubscription(@Param("subscription") Subscription subscription);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSubscription us WHERE us.active = false")
    @Transactional
    void deleteUserSubscriptionsPeriodically();
}
