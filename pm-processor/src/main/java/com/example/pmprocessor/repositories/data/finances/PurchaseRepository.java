package com.example.pmprocessor.repositories.data.finances;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUser(User user);

    List<Purchase> findAllByUserAndApp(User user, App app);

    boolean existsByUserAndApp(User user, App app);
}
