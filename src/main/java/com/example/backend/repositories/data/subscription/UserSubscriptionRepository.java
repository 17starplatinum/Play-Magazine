package com.example.backend.repositories.data.subscription;

import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.user.id = :userId")
    List<Subscription> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT us FROM UserSubscription us WHERE us.subscription.id = :subscriptionId")
    UserSubscription findBySubscriptionId(@Param("subscriptionId") UUID subscriptionId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.user.id = :userId AND us.subscription.id = :subscriptionId")
    Optional<Subscription> findByIdAndUser(UUID subscriptionId, UUID userId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.subscription.id = :subscriptionId AND us.subscription.app.id = :appId")
    Optional<UserSubscription> findBySubscriptionAndApp(UUID subscriptionId, UUID appId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.subscription.app.id = :appId")
    List<Subscription> findByApp(UUID appId);

    @Query("SELECT us.subscription FROM UserSubscription us WHERE us.user.id = :userId AND us.subscription.app.id = :appId")
    List<Subscription> findByUserAndApp(UUID userId, UUID appId);

    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteBySubscription(Subscription subscription);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSubscription us WHERE us.active = false")
    @Transactional
    void deleteUserSubscriptionsPeriodically();
}
