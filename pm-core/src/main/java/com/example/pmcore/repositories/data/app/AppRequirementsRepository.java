package com.example.pmcore.repositories.data.app;

import com.example.pmcore.model.data.app.AppRequirements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppRequirementsRepository extends JpaRepository<AppRequirements, UUID> {

}
