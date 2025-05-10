package com.example.backend.dto.data.app;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppDto {
    private String name;
    private double price;
    private Double averageRating;
}
