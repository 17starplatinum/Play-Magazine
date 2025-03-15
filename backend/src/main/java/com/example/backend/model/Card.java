package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "number", length = 19, nullable = false)
    private String number;

    @Column(name = "cvv", nullable = false)
    private Integer cvv;

    @Column(name = "expired", nullable = false)
    private LocalDate expired;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
