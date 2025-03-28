package com.example.backend.model.data;

import com.example.backend.model.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Purchase {

    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "cost", nullable = false)
    @Min(value = 0L, message = "Сумма должна быть неотрицательным")
    private Float cost;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;
}
