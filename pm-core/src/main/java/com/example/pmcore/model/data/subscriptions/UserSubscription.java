package com.example.pmcore.model.data.subscriptions;

import com.example.pmcore.model.data.finances.Card;
import com.example.pmcore.model.data.finances.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscription {
    @EmbeddedId
    private UserSubscriptionId id;

    @ManyToOne
    @ToString.Exclude
    @MapsId("subscriptionId")
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "is_auto_renewable", nullable = false)
    @Builder.Default
    private Boolean autoRenewal = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = false;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    @JsonIgnore
    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }
}
