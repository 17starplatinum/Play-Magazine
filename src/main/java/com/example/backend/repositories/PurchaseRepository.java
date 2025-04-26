package com.example.backend.repositories;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUser(User user);

    Optional<Purchase> findByUserAndApp(User user, App app);

    boolean existsByUserAndApp(User user, App app);
}
