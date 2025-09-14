package com.example.pmcore.model.data.app;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
@Table(name = "app_versions")
@Getter
@Setter
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
