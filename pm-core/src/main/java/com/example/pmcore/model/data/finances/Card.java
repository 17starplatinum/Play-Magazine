package com.example.pmcore.model.data.finances;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Size(min = 16, max = 19)
    @Pattern(regexp = "\\d+")
    private String number;

    @NotNull
    @Pattern(regexp = "^\\d{3}$")
    private String cvv;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    @Builder.Default
    private double balance = 0;

    @NotNull
    @Builder.Default
    private Boolean isDefault = false;
}
