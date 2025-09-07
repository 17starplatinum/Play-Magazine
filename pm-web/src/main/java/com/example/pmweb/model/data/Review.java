package com.example.pmweb.model.data;

import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.app.App;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @UuidGenerator
    @Column(name = "id")
    @JsonIgnore
    private UUID id;

    @Min(1)
    @Max(5)
    @Column(name = "ratings", nullable = false)
    private int rating;

    @Column(name = "comments")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
