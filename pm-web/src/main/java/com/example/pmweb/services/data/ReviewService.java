package com.example.pmweb.services.data;

import com.example.pmweb.dto.data.review.ReviewInfoDto;
import com.example.pmweb.dto.data.review.ReviewRequestDto;
import com.example.pmweb.exceptions.notfound.ReviewNotFoundException;
import com.example.pmweb.mappers.ReviewMapper;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.Review;
import com.example.pmweb.model.data.app.App;
import com.example.pmweb.repositories.data.ReviewRepository;
import com.example.pmweb.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static com.example.pmweb.model.auth.Role.DEVELOPER;
import static com.example.pmweb.model.auth.Role.USER;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ReviewMapper reviewMapper;

    public UUID createReview(App app, ReviewRequestDto reviewRequestDto) {
        User user = userService.getCurrentUser();

        return reviewRepository.save(
                reviewMapper.mapToModel(app, user, reviewRequestDto)
        ).getId();
    }

    public void deleteReviewById(UUID id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        User user = userService.getCurrentUser();

        if(user.getRole().compare(DEVELOPER) >= 0 || (user.getRole() == USER && review.getUser().equals(user))) {
            reviewRepository.deleteById(id);
            return;
        }
        throw new AccessDeniedException("You are not allowed to delete this review");
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
