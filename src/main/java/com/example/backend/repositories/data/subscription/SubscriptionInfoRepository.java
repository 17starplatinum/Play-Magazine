package com.example.backend.repositories.data.subscription;

import com.example.backend.model.data.subscriptions.SubscriptionInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubscriptionInfoRepository extends JpaRepository<SubscriptionInfo, UUID> {
    List<SubscriptionInfo> findByActiveFalseAndEndDateBefore(LocalDate endDate);
}
