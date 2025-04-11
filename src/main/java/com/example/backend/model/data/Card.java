package com.example.backend.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "number", nullable = false)
    @Size(min = 16, max = 19)
    @Pattern(regexp = "\\d+")
    private String number;

    @Column(name = "cvv", nullable = false)
    @Pattern(regexp = "^\\d{3}$")
    private String cvv;

    @Column(name = "expired", nullable = false)
    private LocalDate expired;

    @Builder.Default
    @Column(name = "balance", nullable = false)
    private double balance = 0;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
