package com.example.backend.dto.data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Min(1)
    @Max(5)
    @Column(name = "stars", nullable = false)
    private int stars;

    @Column(name = "comments")
    private String comment;
}
