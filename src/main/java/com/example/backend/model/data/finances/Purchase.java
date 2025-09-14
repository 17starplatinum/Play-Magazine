package com.example.backend.model.data.finances;

import com.example.backend.model.data.app.App;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Purchase {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private MonetaryTransaction transaction;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PurchaseType purchaseType;

    @NotNull
    @NotBlank
    private String downloadedVersion;
}
