package com.example.backend.services.data;


import com.example.backend.dto.data.ReviewDto;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.ReviewAlreadyExistsException;
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
    private final AppService appService;
    private final ReviewMapper reviewMapper;

    public Review createReview(UUID appId, ReviewDto reviewDto){
        App app = appService.getAppById(appId);

        User user = userService.getCurrentUser();

        if(purchaseService.hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("Вам надо приобрести приложение, прежде чем оставить отзыв на нём");
        }

        if(reviewRepository.existsByUserAndApp(user, app)) {
            throw new ReviewAlreadyExistsException("Вы уже оставили отзыв на этом приложении");
        }

        Review review = reviewMapper.mapToModel(app, user, reviewDto);

        return reviewRepository.save(review);
    }

    public List<Review> getAppReviews(UUID appId) {
        return reviewRepository.findByAppId(appId);
    }

    public double getAverageRating(UUID appId) {
        int ratingCount = reviewRepository.countByAppId(appId);

        if(ratingCount == 0) {
            ratingCount = 1;
        }

        double averageRating = reviewRepository.findRatingsByAppId(appId).orElse(0.0) / ratingCount;
        return new BigDecimal(String.valueOf(averageRating)).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
    }
}
