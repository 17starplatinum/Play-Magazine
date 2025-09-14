package com.example.backend.services.data;

import com.example.backend.dto.data.review.ReviewInfoDto;
import com.example.backend.dto.data.review.ReviewRequestDto;
import com.example.backend.exceptions.notfound.ReviewNotFoundException;
import com.example.backend.mappers.ReviewMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import com.example.backend.repositories.data.ReviewRepository;
import com.example.backend.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static com.example.backend.model.auth.Role.DEVELOPER;
import static com.example.backend.model.auth.Role.USER;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ReviewMapper reviewMapper;

    public UUID createReview(App app, ReviewRequestDto reviewRequestDto) {
        UUID userId = userService.getCurrentUserId();

        return reviewRepository.save(
                reviewMapper.mapToModel(app, userId, reviewRequestDto)
        ).getId();
    }

    public void deleteReviewById(UUID id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        User user = userService.getCurrentUser();

        if(user.getRole().compare(DEVELOPER) >= 0 || (user.getRole() == USER && review.getUserId().equals(user.getId()))) {
            reviewRepository.deleteById(id);
            return;
        }
        throw new AccessDeniedException("You are not allowed to delete this review");
    }

    public List<ReviewInfoDto> getAppReviews(UUID appId) {
        return reviewRepository.findByAppId(appId)
                .stream()
                .map(review -> reviewMapper.mapToDto(review, userService.getById(review.getUserId())))
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
