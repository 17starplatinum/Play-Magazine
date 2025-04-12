package com.example.backend.model.data.subscriptions;


import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscription_infos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionInfo {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Positive
    @NotNull
    private Integer days;

    @Builder.Default
    @Column(name = "is_auto_renewable", nullable = false)
    private Boolean autoRenewal = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = false;
}
