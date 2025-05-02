package com.example.backend.model.data.subscriptions;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Builder
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @UuidGenerator
    private UUID id;

    @NotNull
    @NotBlank
    private String name;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subscription_info_id", nullable = false)
    private SubscriptionInfo subscriptionInfo;
}
