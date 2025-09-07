package com.example.pmweb.mappers;

import com.example.pmweb.dto.data.review.ReviewInfoDto;
import com.example.pmweb.dto.data.review.ReviewRequestDto;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.Review;
import com.example.pmweb.model.data.app.App;
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
