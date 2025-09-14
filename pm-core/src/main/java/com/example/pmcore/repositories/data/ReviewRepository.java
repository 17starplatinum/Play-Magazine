package com.example.pmcore.repositories.data;

import com.example.backend.model.data.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByAppId(UUID appId);

    @Query("SELECT r.rating FROM Review r WHERE r.app.id = :appId")
    List<Double> findRatingsByAppId(@Param("appId") UUID appId);

    int countReviewByAppId(@Param("appId") UUID appId);
}
