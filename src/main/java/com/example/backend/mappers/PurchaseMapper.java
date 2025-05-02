package com.example.backend.mappers;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.finances.MonetaryTransaction;
import com.example.backend.model.data.finances.Purchase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PurchaseMapper {
    public Purchase mapToModel(App app, MonetaryTransaction transaction, User user) {
        return Purchase.builder()
                .app(app)
                .user(user)
                .transaction(transaction)
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
