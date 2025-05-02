package com.example.backend.model.data.app;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.subscriptions.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "apps")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class App {

    @Id
    @UuidGenerator
    private UUID id;

    @NotBlank
    @Column(length = 32, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @PositiveOrZero
    private Double price;

    @NotNull
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @JsonIgnore
    @OneToOne
    private AppFile appFile;

    @JsonIgnore
    @ManyToMany(mappedBy = "downloadedApps")
    private Set<User> usersWhoDownloaded;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    List<AppVersion> appVersions = new ArrayList<>();

    public boolean hasSubscriptions() {
        return !subscriptions.isEmpty();
    }

    public boolean isNewerThan(App app) {
        return app.getId().equals(this.id) && app.appVersions.size() < this.appVersions.size();
    }

    public AppVersion getLatestVersion() {
        return appVersions.get(appVersions.size() - 1);
    }

    public AppVersion getPreviousVersion() {
        return appVersions.size() == 1 ?
                null : appVersions.get(appVersions.size() - 2);
    }


}
