package com.example.backend.dto.data.review;

import com.example.backend.model.data.Review;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ReviewResponseDto {
    private final String app;
    private final Double averageRating;
    private final List<ReviewInfoDto> reviews;

    public ReviewResponseDto(String app, Double averageRating, List<Review> reviews) {
        this.app = app;
        this.averageRating = averageRating;
        this.reviews = new ArrayList<>();
        for (Review review : reviews) this.reviews.add(Review.fromReviewToDto(review));
    }
}
