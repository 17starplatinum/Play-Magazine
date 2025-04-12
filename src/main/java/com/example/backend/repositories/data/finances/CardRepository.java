package com.example.backend.repositories.data.finances;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.finances.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUser(User user);
    Optional<Card> findByIdAndUser(UUID id, User user);


    boolean existsByUserAndNumber(User user, String number);

    boolean existsByIdAndUser(UUID cardId, User user);
    @Modifying
    @Query("UPDATE Card c SET c.isDefault = false WHERE c.user.id = :userId")
    void clearDefaultFlags(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Card c SET c.isDefault = true WHERE c.id = :cardId")
    void setDefaultCard(@Param("cardId") UUID cardId);
}
