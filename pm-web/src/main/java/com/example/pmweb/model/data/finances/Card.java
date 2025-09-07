package com.example.pmweb.model.data.finances;

import com.example.pmweb.model.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

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
