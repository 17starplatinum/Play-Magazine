package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class App {

    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "version", nullable = false)
    private Float version;

    @ManyToMany(mappedBy = "downloadedApps", fetch = FetchType.LAZY)
    private Set<User> usersWhoDownloaded;
}
