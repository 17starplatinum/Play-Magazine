package com.example.backend.model.data.subscriptions;

import com.example.backend.model.data.app.App;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
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
    @JoinColumn(name = "app_id")
    private App app;

    @JsonIgnore
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserSubscription> subscribedUser = new HashSet<>();
}
