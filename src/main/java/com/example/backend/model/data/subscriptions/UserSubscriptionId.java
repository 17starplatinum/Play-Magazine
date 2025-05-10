package com.example.backend.model.data.subscriptions;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Builder
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Column(name = "subscription_id")
    private UUID subscriptionId;
}
