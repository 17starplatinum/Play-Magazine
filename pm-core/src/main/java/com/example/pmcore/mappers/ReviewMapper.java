package com.example.pmcore.mappers;

import com.example.pmcore.dto.data.review.ReviewInfoDto;
import com.example.pmcore.dto.data.review.ReviewRequestDto;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.data.Review;
import com.example.pmcore.model.data.app.App;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewMapper {
    public Review mapToModel(App app, UUID userId, ReviewRequestDto reviewRequestDto) {
        return Review.builder()
                .rating(reviewRequestDto.getStars())
                .comment(reviewRequestDto.getComment())
                .app(app)
                .userId(userId)
                .build();
    }
    public ReviewInfoDto mapToDto(Review review, User author) {
        return ReviewInfoDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .author(author.getEmail())
                .createdAt(review.getCreatedAt().toLocalDate())
                .build();
    }
}
