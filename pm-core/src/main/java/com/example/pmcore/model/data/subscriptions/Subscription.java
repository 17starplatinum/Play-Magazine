package com.example.pmcore.model.data.subscriptions;

import com.example.pmcore.model.data.app.App;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
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
    @JoinColumn(name = "app_id")
    private App app;

    @Positive
    @NotNull
    private Integer days;

    @Positive
    @NotNull
    private double price;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserSubscription> userSubscriptions = new HashSet<>();
}
