package com.example.backend.services.data;


import com.example.backend.dto.data.ReviewDto;
import com.example.backend.exceptions.prerequisites.ReviewAlreadyExistsException;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Review;
import com.example.backend.repositories.AppRepository;
import com.example.backend.repositories.ReviewRepository;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final PurchaseService purchaseService;

    public Review createReview(UUID appId, ReviewDto reviewDto, UserDetails currentUser){
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));

        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        if(purchaseService.hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("Вам надо приобрести приложение, прежде чем оставить отзыв на нём", new RuntimeException());
        }

        if(reviewRepository.existsByUserAndApp(user, app)) {
            throw new ReviewAlreadyExistsException("Вы уже оставили отзыв на этом приложении", new RuntimeException());
        }

        Review review = Review.builder()
                .rating(reviewDto.getStars())
                .comment(reviewDto.getComment())
                .app(app)
                .user(user)
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getAppReviews(UUID appId) {
        return reviewRepository.findByAppId(appId);
    }

    public double getAverageRating(UUID appId) {
        return reviewRepository.findAverageRatingByAppId(appId).orElse(0.0);
    }
}
