package com.example.backend.model.data.app;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
@Table(name = "app_versions")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class AppVersion {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private App app;

    @NonNull
    @NotBlank
    private String version;

    @NotBlank
    private String releaseNotes;

    public AppVersion(@NotNull String version, String releaseNotes) {
        this.version = version;
        this.releaseNotes = releaseNotes;
    }
}
