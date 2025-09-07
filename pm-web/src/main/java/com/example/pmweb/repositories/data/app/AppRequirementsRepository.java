package com.example.pmweb.repositories.data.app;

import com.example.pmweb.model.data.app.AppRequirements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppRequirementsRepository extends JpaRepository<AppRequirements, UUID> {

}
