package com.example.backend.model.auth;

import com.example.backend.services.util.LocalDateAdapter;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
@XmlAccessorType(XmlAccessType.FIELD)
public class UserBudget {
    @Id
    @UuidGenerator
    private UUID id;

    @DecimalMin("0.00")
    private Double spendingLimit;

    @Builder.Default
    private Double currentSpending = 0D;

    @Builder.Default
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastLimitReset = LocalDate.now();
}
