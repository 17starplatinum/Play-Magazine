package com.example.pmweb.dto.data.review;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
public class ReviewInfoDto {
    private UUID id;
    private int rating;
    private String comment;
    private String author;
    private LocalDate createdAt;
}
