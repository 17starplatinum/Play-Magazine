package com.example.backend.mappers;

import com.example.backend.dto.data.review.ReviewInfoDto;
import com.example.backend.dto.data.review.ReviewRequestDto;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public Review mapToModel(App app, User user, ReviewRequestDto reviewRequestDto) {
        return Review.builder()
                .rating(reviewRequestDto.getStars())
                .comment(reviewRequestDto.getComment())
                .app(app)
                .user(user)
                .build();
    }
    public ReviewInfoDto mapToDto(Review review) {
        return ReviewInfoDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .author(review.getUser().getEmail())
                .createdAt(review.getCreatedAt().toLocalDate())
                .build();
    }
}
