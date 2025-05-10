package com.example.backend.dto.data.review;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class ReviewInfoDto {
    private int rating;
    private String comment;
    private String author;
    private LocalDate createdAt;
}
