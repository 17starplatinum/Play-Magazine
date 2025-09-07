package com.example.pmweb.mappers;

import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.app.App;
import com.example.pmweb.model.data.finances.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PurchaseMapper {
    public Purchase mapToModel(PurchaseType purchaseType, App app, MonetaryTransaction transaction, User user) {
        return Purchase.builder()
                .app(app)
                .user(user)
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
