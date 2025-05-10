package com.example.backend.model.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_budgets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBudget {
    @Id
    @UuidGenerator
    private UUID id;

    @DecimalMin("0.00")
    private Double spendingLimit;

    @Builder.Default
    private Double currentSpending = 0D;

    @Builder.Default
    private LocalDate lastLimitReset = LocalDate.now();
}
