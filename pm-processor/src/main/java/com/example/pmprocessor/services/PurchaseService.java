package com.example.pmprocessor.services;

import com.example.pmcommon.dto.async.PurchaseMessage;
import com.example.pmcommon.model.Operation;
import com.example.pmcommon.model.OperationStatus;
import com.example.pmprocessor.repositories.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseServiceInternal purchaseServiceInternal;
    private final OperationRepository operationRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;

    public void processPurchase(PurchaseMessage message) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        try {
            Operation operation = operationRepository.findById(UUID.fromString(message.getOperationId()))
                    .orElseThrow(() -> new RuntimeException("Operation not found"));
            operation.setStatus(OperationStatus.IN_PROGRESS);
            operation.setUpdatedAt(LocalDateTime.now());
            operationRepository.save(operation);

            purchaseServiceInternal.processPurchase(message.getAppId(), message.getCardId());

            operation.setStatus(OperationStatus.COMPLETE);
            operation.setUpdatedAt(LocalDateTime.now());
            operationRepository.save(operation);
            transactionManager.commit(transaction);
        } catch (Exception e) {
            Operation operation = operationRepository.findById(UUID.fromString(message.getOperationId()))
                    .orElseThrow(() -> new RuntimeException("Operation not found"));
            operation.setStatus(OperationStatus.FAILED);
            operation.setUpdatedAt(LocalDateTime.now());
            operation.setDetails(e.getMessage());
            operationRepository.save(operation);
            transactionManager.rollback(transaction);
            throw e;
        }
    }
}
