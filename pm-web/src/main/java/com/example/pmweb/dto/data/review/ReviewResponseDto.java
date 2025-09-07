package com.example.pmweb.dto.data.review;

import java.util.List;

public record ReviewResponseDto(String app, Double averageRating, List<ReviewInfoDto> reviews) {
}
