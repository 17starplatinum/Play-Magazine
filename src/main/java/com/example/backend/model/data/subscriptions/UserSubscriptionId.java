package com.example.backend.model.data.subscriptions;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
public class UserSubscriptionId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Column(name = "subscription_id")
    private UUID subscriptionId;
}
