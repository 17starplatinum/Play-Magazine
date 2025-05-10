package com.example.backend.services.data;

import com.example.backend.dto.data.review.ReviewInfoDto;
import com.example.backend.dto.data.review.ReviewRequestDto;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.mappers.ReviewMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import com.example.backend.repositories.data.ReviewRepository;
import com.example.backend.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PurchaseService purchaseService;
    private final UserService userService;
    private final ReviewMapper reviewMapper;

    public UUID createReview(App app, ReviewRequestDto reviewRequestDto) {
        User user = userService.getCurrentUser();

        if (purchaseService.hasUserPurchasedApp(user, app))
            throw new AppNotPurchasedException("You need to purchase the app before you can leave a review.");

        return reviewRepository.save(
                reviewMapper.mapToModel(app, user, reviewRequestDto)
        ).getId();
    }

    public void deleteReviews(List<Review> reviews) {
        reviewRepository.deleteAll(reviews);
    }

    public List<ReviewInfoDto> getAppReviews(UUID appId) {
        return reviewRepository.findByAppId(appId)
                .stream()
                .map(reviewMapper::mapToDto)
                .toList();
    }

    public Double getAverageRating(UUID appId) {
        int ratingCount = reviewRepository.countReviewByAppId(appId);
        if (ratingCount == 0) ratingCount = 1;

        double averageRating = reviewRepository.findRatingsByAppId(appId)
                .stream()
                .reduce(0.0, Double::sum) / ratingCount;

        return new BigDecimal(String.valueOf(averageRating))
                .setScale(1, RoundingMode.HALF_EVEN)
                .doubleValue();
    }
}
