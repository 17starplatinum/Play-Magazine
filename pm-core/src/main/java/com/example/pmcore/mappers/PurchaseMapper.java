package com.example.pmcore.mappers;

import com.example.pmcore.model.data.app.App;
import com.example.pmcore.model.data.finances.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PurchaseMapper {
    public Purchase mapToModel(PurchaseType purchaseType, App app, MonetaryTransaction transaction, UUID userId) {
        return Purchase.builder()
                .app(app)
                .userId(userId)
                .purchaseType(purchaseType)
                .transaction(transaction)
                .downloadedVersion(app.getLatestVersion().getVersion())
                .build();
    }

    public MonetaryTransaction mapToTransaction(Card card, Invoice invoice) {
        return MonetaryTransaction.builder()
                .card(card)
                .invoice(invoice)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public Invoice mapToInvoice(double price) {
        return Invoice.builder()
                .amount(price)
                .build();
    }
}
