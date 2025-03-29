package com.example.backend.model.data;

import com.example.backend.model.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Column(name = "id")
    private UUID id;

    @NotBlank
    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Min(value = 0L, message = "Цена должна быть неотрицательным")
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

    @Column(name = "file_paths", nullable = false)
    private String filePath;

    @Positive
    @Column(name = "min_ram_mb", nullable = false)
    private Integer minRamMb;

    @Positive
    @Column(name = "min_storage_mb", nullable = false)
    private Integer minStorageMb;

    @NotBlank
    @Column(name = "os_requirements", nullable = false)
    private String osRequirements;

    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Purchase> purchases = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @ManyToMany(mappedBy = "downloadedApps", fetch = FetchType.LAZY)
    private Set<User> usersWhoDownloaded;

    public boolean isFree() {
        return price == 0;
    }
}
