package com.example.backend.mappers;

import com.example.backend.dto.data.ReviewDto;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public Review mapToModel(App app, User user, ReviewDto reviewDto) {
        return Review.builder()
                .rating(reviewDto.getStars())
                .comment(reviewDto.getComment())
                .app(app)
                .user(user)
                .build();
    }
}
