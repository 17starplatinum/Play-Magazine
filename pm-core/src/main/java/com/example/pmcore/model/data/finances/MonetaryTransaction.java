package com.example.pmcore.model.data.finances;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "monetary_transactions")
@Getter
@Setter
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

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Purchase> purchases;
}
