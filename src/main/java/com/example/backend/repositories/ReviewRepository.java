package com.example.backend.repositories;

import com.example.backend.model.data.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.data.App;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByAppId(UUID appId);
    boolean existsByUserAndApp(User user, App app);
    Optional<Double> findAverageRatingByAppId(UUID appId);
}
