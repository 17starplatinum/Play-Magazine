package com.example.pmcore.dto.data.review;

import java.util.List;

public record ReviewResponseDto(String app, Double averageRating, List<ReviewInfoDto> reviews) {
}
