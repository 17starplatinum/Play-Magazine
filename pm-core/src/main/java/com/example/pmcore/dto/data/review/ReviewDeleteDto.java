package com.example.pmcore.dto.data.review;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class ReviewDeleteDto {
    private UUID appId;
    private UUID reviewId;
}
