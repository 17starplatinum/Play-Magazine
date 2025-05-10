package com.example.backend.model.data.subscriptions;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

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
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne
    @MapsId("subscriptionId")
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Positive
    @NotNull
    private Integer days;

    @Column(name = "is_auto_renewable", nullable = false)
    @Builder.Default
    private Boolean autoRenewal = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = false;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
}
