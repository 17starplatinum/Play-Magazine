package com.example.pmprocessor.listeners;

import com.example.pmcommon.dto.async.PurchaseMessage;
import com.example.pmcommon.model.Operation;
import com.example.pmcommon.model.OperationStatus;
import com.example.pmprocessor.repositories.OperationRepository;
import com.example.pmprocessor.services.PurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@RequiredArgsConstructor
public class PurchaseMessageListener {
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;
    private final OperationRepository operationRepository;
    private final PurchaseService purchaseService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "purchases")
    public void processPurchase(Message message) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        try {
            String payload = ((TextMessage) message).getText();
            PurchaseMessage purchaseMessage = objectMapper.readValue(payload, PurchaseMessage.class);

            purchaseService.processPurchase(purchaseMessage);

            Operation operation = operationRepository.findById(purchaseMessage.getOperationId())
                    .orElseThrow(() -> new RuntimeException("Operation not found"));
            operation.setStatus(OperationStatus.COMPLETE);
            operation.setDetails("Purchase completed successfully");
            message.acknowledge();
            transactionManager.commit(transaction);
        } catch (JMSException e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException("Failed to process purchase message");
        } catch (Exception e) {
            try {
                String payload = ((TextMessage) message).getText();
                PurchaseMessage purchaseMessage = objectMapper.readValue(payload, PurchaseMessage.class);
                Operation operation = operationRepository.findById(purchaseMessage.getOperationId())
                        .orElseThrow(() -> new RuntimeException("Operation not found"));
                operation.setStatus(OperationStatus.FAILED);
                operation.setDetails(e.getMessage());
                operationRepository.save(operation);
                transactionManager.commit(transaction);
            } catch (Exception ex) {
                transactionManager.rollback(transaction);
            }
            throw new RuntimeException("Error during purchase processing", e);
        }
    }
}
