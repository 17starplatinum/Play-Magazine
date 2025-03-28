package com.example.backend.repositories;

import com.example.backend.model.data.Card;
import com.example.backend.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUserAndDeletedFalse(User user);
    Optional<Card> findByIdAndUser(UUID id, User user);
    boolean existsByUserAndNumberAndDeletedFalse(User user, String number);
}
