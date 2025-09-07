package com.example.pmprocessor.repositories.data.app;

import com.example.backend.model.data.app.AppFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppFileRepository extends JpaRepository<AppFile, UUID> {

}
