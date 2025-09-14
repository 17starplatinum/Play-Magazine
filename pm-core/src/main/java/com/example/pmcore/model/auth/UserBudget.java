package com.example.pmcore.model.auth;

import com.example.pmcore.services.util.LocalDateAdapter;
import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class UserBudget implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "id", nullable = false)
    @XmlElement(name = "id")
    private UUID id;

    @DecimalMin("0.00")
    private Double spendingLimit;

    @Builder.Default
    private Double currentSpending = 0D;

    @Builder.Default
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastLimitReset = LocalDate.now();
}
