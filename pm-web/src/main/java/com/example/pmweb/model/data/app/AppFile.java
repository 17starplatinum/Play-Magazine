package com.example.pmweb.model.data.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_files")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppFile {
    @Id
    @UuidGenerator
    private UUID id;

    private LocalDateTime lastUpdated;

    @NotNull
    private String filePath;

    private Long fileSize;

    private String fileHash;
}
