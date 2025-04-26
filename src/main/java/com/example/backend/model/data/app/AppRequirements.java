package com.example.backend.model.data.app;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_requirements")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRequirements {
    @Id
    @UuidGenerator
    private UUID id;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "app_id")
    private App app;

    @Positive
    @NotNull
    private Integer minRamMb;

    @Positive
    @NotNull
    private Integer minStorageMb;

    @NotEmpty
    @NotNull
    @ElementCollection
    private List<String> compatibleOs;
}
