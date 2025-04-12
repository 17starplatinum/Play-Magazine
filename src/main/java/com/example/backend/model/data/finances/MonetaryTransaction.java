package com.example.backend.model.data.finances;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "monetary_transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonetaryTransaction {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false)
    private Card card;

    @ManyToOne
    private Invoice invoice;

    @NotNull
    private LocalDateTime processedAt;

    @OneToMany
    private List<Purchase> purchases;
}
