package com.example.backend.dto.data;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    @Min(1)
    @Max(5)
    @Column(name = "stars", nullable = false)
    private int stars;

    @Column(name = "comments")
    private String comment;
}
