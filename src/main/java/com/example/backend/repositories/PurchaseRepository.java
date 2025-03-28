package com.example.backend.repositories;

import com.example.backend.model.data.App;
import com.example.backend.model.data.Purchase;
import com.example.backend.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUser(User user);
    boolean existsByUserAndApp(User user, App app);
}
