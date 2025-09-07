package com.example.pmprocessor.services;

import com.example.pmcommon.dto.async.SubscriptionMessage;
import com.example.pmcommon.model.Operation;
import com.example.pmcommon.model.OperationStatus;
import com.example.pmprocessor.repositories.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionServiceInternal subscriptionServiceInternal;
    private final OperationRepository operationRepository;
    private final DefaultTransactionDefinition transactionDefinition;
    private final PlatformTransactionManager transactionManager;

    public void processSubscription(SubscriptionMessage message) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Operation operation = operationRepository.findById(message.getOperationId())
                    .orElseThrow(() -> new RuntimeException("Operation not found"));
            operation.setStatus(OperationStatus.IN_PROGRESS);
            operation.setUpdatedAt(LocalDateTime.now());
            operationRepository.save(operation);
            switch (message.getAction()) {
                case CREATE:
                    subscriptionServiceInternal.createSubscription(message.getSubscriptionId(), message.getUserId());
                    break;
                case CANCEL:
                    subscriptionServiceInternal.cancelSubscription(message.getSubscriptionId(), message.getUserId());
                    break;
                case RENEW:
                    subscriptionServiceInternal.renewSubscription(message.getSubscriptionId(), message.getUserId());
                    break;
            }

            operation.setStatus(OperationStatus.COMPLETE);
            operation.setUpdatedAt(LocalDateTime.now());
            operationRepository.save(operation);
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            Operation operation = operationRepository.findById(message.getOperationId())
                    .orElseThrow(() -> new RuntimeException("Operation not found"));
            operation.setStatus(OperationStatus.FAILED);
            operation.setUpdatedAt(LocalDateTime.now());
            operation.setDetails(e.getMessage());
            operationRepository.save(operation);
            transactionManager.commit(transaction);
            throw e;
        }
    }
}
