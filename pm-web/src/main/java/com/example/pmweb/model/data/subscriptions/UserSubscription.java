package com.example.pmweb.model.data.subscriptions;

import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.finances.Card;
import com.example.pmweb.model.data.finances.Invoice;
import jakarta.persistence.*;
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

    @ManyToOne
    @ToString.Exclude
    @MapsId("subscriptionId")
    @JoinColumn(name = "subscription_id")
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
}
