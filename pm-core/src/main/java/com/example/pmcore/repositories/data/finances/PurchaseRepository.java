package com.example.pmcore.repositories.data.finances;

import com.example.pmcore.model.data.app.App;
import com.example.pmcore.model.data.finances.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserId(UUID userId);

    List<Purchase> findAllByUserIdAndApp(UUID userId, App app);

    boolean existsByUserIdAndApp(UUID userId, App app);
}
