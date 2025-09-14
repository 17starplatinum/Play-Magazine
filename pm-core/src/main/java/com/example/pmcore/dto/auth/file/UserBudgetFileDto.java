package com.example.pmcore.dto.auth.file;

import com.example.backend.services.util.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@XmlRootElement(name = "userBudget")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBudgetFileDto {
    @XmlElement
    private UUID id;

    @XmlElement
    private Double spendingLimit;

    @XmlElement
    private Double currentSpending;

    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastLimitReset;
}
