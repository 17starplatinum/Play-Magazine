package com.example.backend.repositories.data;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByAppId(UUID appId);
    boolean existsByUserAndApp(User user, App app);

    @Query("select r.rating from Review r where r.app.id = :appId")
    List<Double> findRatingsByAppId(@Param("appId") UUID appId);

    // @Query(value = "select count(*) from reviews r where r.app_id = :appId", nativeQuery = true)
    int countReviewByAppId(@Param("appId") UUID appId);
}
