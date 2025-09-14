package com.example.pmcore.dto.data.app;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AppInfoResponseDto {
    private String name;
    private String description;
    private double price;
    private LocalDate releaseDate;
    private String latestVersion;
    private String releaseNotes;
    private String ownerEmail;
    private Double averageRating;
}