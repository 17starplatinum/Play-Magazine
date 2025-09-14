package com.example.backend.model.data.app;

import com.example.backend.model.data.Review;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.services.util.LocalDateAdapter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "apps")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
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
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate releaseDate;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Purchase> purchases;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AppFile appFile;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AppRequirements appRequirements;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserAppDownload> userAppDownloads = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    List<AppVersion> appVersions = new ArrayList<>();

    public AppVersion getLatestVersion() {
        return appVersions.get(appVersions.size() - 1);
    }
}
