package com.example.pmcore.model.data.app;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_requirements")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRequirements {
    @Id
    @UuidGenerator
    private UUID id;

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
