package com.example.pmweb.services.data;

import com.example.pmweb.exceptions.notfound.OperationNotFoundException;
import com.example.pmcommon.model.Operation;
import com.example.pmcommon.model.OperationStatus;
import com.example.pmcommon.model.OperationType;
import com.example.pmweb.repositories.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationService {
    private final OperationRepository operationRepository;
    public Operation createOperation(OperationType type, OperationStatus status) {
        UUID operationId = UUID.randomUUID();
        Operation operation = Operation.builder()
                .id(operationId)
                .type(type)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return operationRepository.save(operation);
    }

    public Operation updateOperationStatus(UUID operationId, OperationStatus status, String details) {
        Operation operation = getOperationById(operationId);
        operation.setStatus(status);
        operation.setUpdatedAt(LocalDateTime.now());
        operation.setDetails(details);
        return operationRepository.save(operation);
    }

    public Operation getOperationById(UUID operationId) {
        return operationRepository.findById(operationId)
                .orElseThrow(() -> new OperationNotFoundException("Operation not found"));
    }
}
