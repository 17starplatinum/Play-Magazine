package com.example.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUser(User user);
    Optional<Card> findByIdAndUser(UUID id, User user);

    Optional<Card> findByNumberAndUser(String number, User user);

    boolean existsByUserAndNumber(User user, String number);
}
