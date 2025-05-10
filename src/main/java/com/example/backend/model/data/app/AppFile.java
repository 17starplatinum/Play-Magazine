package com.example.backend.model.data.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_files")
@Data
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
