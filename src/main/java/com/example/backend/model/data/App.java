package com.example.backend.model.data;

import com.example.backend.model.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
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

    @ManyToMany(mappedBy = "downloadedApps", fetch = FetchType.LAZY)
    private Set<User> usersWhoDownloaded;
}
