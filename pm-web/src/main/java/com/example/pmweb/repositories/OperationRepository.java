package com.example.pmweb.repositories;

import com.example.pmcommon.model.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID> {
    Optional<Operation> findById(@NotNull UUID operationId);
}
