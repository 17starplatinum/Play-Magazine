package com.example.backend.dto.data.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewResponseDto {
    private final String app;
    private final Double averageRating;
    private final List<ReviewInfoDto> reviews;
}
